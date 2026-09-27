package com.danesh.sadad.charge

import com.danesh.api.ChargeKind
import com.danesh.api.ChargeOperator
import com.danesh.api.ChargeProduct

data class SadadChargeListSnapshot(
    val voucherOperators: List<ChargeOperator>,
    val topUpOperators: List<ChargeOperator>,
    val products: List<ChargeProduct>,
)

internal object SadadChargeListParser {

    fun parse(input: java.io.InputStream): SadadChargeListSnapshot {
        var bytes = input.readBytes()
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) {
            bytes = bytes.copyOfRange(3, bytes.size)
        }
        val document = XmlNode.parse(bytes.toString(Charsets.UTF_8))
        val voucherOperators = mutableListOf<ChargeOperator>()
        val topUpOperators = mutableListOf<ChargeOperator>()
        val products = mutableListOf<ChargeProduct>()
        document.descendants("MenuItem").forEach { item ->
            when (item.attr("class")) {
                "normal_charge" -> parseKind(item, ChargeKind.VOUCHER, voucherOperators, products)
                "topup_charge" -> parseKind(item, ChargeKind.TOPUP, topUpOperators, products)
            }
        }
        return SadadChargeListSnapshot(voucherOperators, topUpOperators, products)
    }

    private fun parseKind(
        root: XmlNode,
        kind: ChargeKind,
        operators: MutableList<ChargeOperator>,
        products: MutableList<ChargeProduct>,
    ) {
        root.childMenuItems().forEach { operatorNode ->
            if (operatorNode.attr("class") != "operator") return@forEach
            if (!operatorNode.isEnabled()) return@forEach
            val collected = mutableListOf<ChargeProduct>()
            operatorNode.childMenuItems().forEach { child ->
                collectProducts(child, kind, groupLabel = "", groupLabelEn = "", collected)
            }
            val providerId = collected.firstOrNull()?.providerId.orEmpty()
            if (providerId.isBlank() || collected.isEmpty()) return@forEach
            operators += ChargeOperator(
                providerId = providerId,
                nameFa = operatorNode.attr("fn"),
                nameEn = operatorNode.attr("en"),
                ussdChargeCommand = operatorNode.attr("usssdChargeCommand")
                    .ifBlank { operatorNode.attr("ussdChargeCommand") },
                ussdGetSimCharge = operatorNode.attr("ussdGetSimCharge"),
                taxPercent = operatorNode.attr("taxPercent").toIntOrNull() ?: 0,
                minChargeAmount = operatorNode.attr("minChargeAmount").toLongOrNull(),
            )
            products += collected
        }
    }

    private fun collectProducts(
        node: XmlNode,
        kind: ChargeKind,
        groupLabel: String,
        groupLabelEn: String,
        out: MutableList<ChargeProduct>,
    ) {
        if (!node.isEnabled()) return
        val descriptor = node.children.firstOrNull { it.tag == "ServiceDescriptor" }
        val nested = node.childMenuItems()
        if (descriptor != null) {
            val amountRaw = descriptor.attr("serviceAmount")
            out += ChargeProduct(
                id = descriptor.attr("id"),
                kind = kind,
                providerId = descriptor.attr("providerId"),
                amountRials = amountRaw.toLongOrNull(),
                categoryId = descriptor.attr("categoryId")
                    .ifBlank { descriptor.attr("topUpCategoryType") },
                serviceTypeCode = descriptor.attr("serviceTypeCode"),
                loadUssd = descriptor.attr("load"),
                labelFa = node.attr("fn"),
                groupLabelFa = groupLabel,
                labelEn = node.attr("en"),
                groupLabelEn = groupLabelEn,
                hasCount = when (descriptor.attr("hasCount").lowercase()) {
                    "false" -> false
                    else -> true
                },
            )
            return
        }
        val nextGroup = node.attr("fn")
        val nextGroupEn = node.attr("en")
        nested.forEach { child ->
            collectProducts(child, kind, groupLabel = nextGroup, groupLabelEn = nextGroupEn, out)
        }
    }
}

/**
 * خواندن ChargeList بدون `DocumentBuilder`.
 * `DocumentBuilderFactory.newInstance()` پارسر را از `META-INF/services` برمی‌دارد
 * (معمولاً Xerces کنار jPOS). در ریلیز R8 آن کلاس را حذف می‌کند و باز شدن
 * شارژ موبایل و کد شارژ با `FactoryConfigurationError` می‌افتد.
 */
private class XmlNode(
    val tag: String,
    private val attributes: Map<String, String>,
    val children: List<XmlNode>,
) {
    fun attr(name: String): String = attributes[name].orEmpty()

    fun childMenuItems(): List<XmlNode> = children.filter { it.tag == "MenuItem" }

    fun isEnabled(): Boolean = attr("enable").lowercase() != "false"

    fun descendants(tag: String): List<XmlNode> {
        val out = mutableListOf<XmlNode>()
        fun walk(node: XmlNode) {
            node.children.forEach { child ->
                if (child.tag == tag) out += child
                walk(child)
            }
        }
        walk(this)
        return out
    }

    companion object {
        fun parse(xml: String): XmlNode {
            val start = xml.indexOf('<')
            require(start >= 0) { "ChargeList XML is empty" }
            return readNode(xml, start).node
        }

        private data class Cursor(val node: XmlNode, val next: Int)

        private fun readNode(xml: String, start: Int): Cursor {
            var index = skipProlog(xml, start)
            require(index < xml.length && xml[index] == '<') { "ChargeList XML has no root element" }
            val tagEnd = xml.indexOf('>', index)
            require(tagEnd > index) { "ChargeList XML has an unclosed tag" }
            val raw = xml.substring(index + 1, tagEnd).trim()
            val selfClosing = raw.endsWith("/")
            val header = if (selfClosing) raw.dropLast(1).trim() else raw
            val (name, attributes) = splitTag(header)
            if (selfClosing) {
                return Cursor(XmlNode(name, attributes, emptyList()), tagEnd + 1)
            }
            val children = mutableListOf<XmlNode>()
            var cursor = tagEnd + 1
            while (cursor < xml.length) {
                val next = xml.indexOf('<', cursor)
                if (next < 0) break
                if (xml.startsWith("</", next)) {
                    val closeEnd = xml.indexOf('>', next)
                    return Cursor(XmlNode(name, attributes, children), closeEnd + 1)
                }
                if (xml.startsWith("<?", next) || xml.startsWith("<!--", next)) {
                    cursor = skipProlog(xml, next)
                    continue
                }
                val child = readNode(xml, next)
                children += child.node
                cursor = child.next
            }
            return Cursor(XmlNode(name, attributes, children), xml.length)
        }

        private fun skipProlog(xml: String, start: Int): Int {
            var index = start
            while (index < xml.length) {
                if (xml.startsWith("<?", index)) {
                    val end = xml.indexOf("?>", index)
                    index = if (end < 0) xml.length else end + 2
                    continue
                }
                if (xml.startsWith("<!--", index)) {
                    val end = xml.indexOf("-->", index)
                    index = if (end < 0) xml.length else end + 3
                    continue
                }
                if (xml[index].isWhitespace()) {
                    index++
                    continue
                }
                break
            }
            return index
        }

        private fun splitTag(header: String): Pair<String, Map<String, String>> {
            val nameEnd = header.indexOfFirst { it.isWhitespace() }
            if (nameEnd < 0) return header to emptyMap()
            val name = header.substring(0, nameEnd)
            val attributes = linkedMapOf<String, String>()
            var index = nameEnd
            while (index < header.length) {
                while (index < header.length && header[index].isWhitespace()) index++
                if (index >= header.length) break
                val equals = header.indexOf('=', index)
                require(equals > index) { "ChargeList attribute is missing a value" }
                val key = header.substring(index, equals).trim()
                var valueStart = equals + 1
                while (valueStart < header.length && header[valueStart].isWhitespace()) valueStart++
                val quote = header[valueStart]
                require(quote == '"' || quote == '\'') { "ChargeList attribute is not quoted" }
                val valueEnd = header.indexOf(quote, valueStart + 1)
                require(valueEnd > valueStart) { "ChargeList attribute is not closed" }
                attributes[key] = unescape(header.substring(valueStart + 1, valueEnd))
                index = valueEnd + 1
            }
            return name to attributes
        }

        private fun unescape(value: String): String =
            value.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&amp;", "&")
    }
}
