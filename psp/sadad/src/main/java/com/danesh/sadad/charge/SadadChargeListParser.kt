package com.danesh.sadad.charge

import com.danesh.api.ChargeKind
import com.danesh.api.ChargeOperator
import com.danesh.api.ChargeProduct
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

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
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(java.io.ByteArrayInputStream(bytes))
        document.documentElement.normalize()
        val voucherOperators = mutableListOf<ChargeOperator>()
        val topUpOperators = mutableListOf<ChargeOperator>()
        val products = mutableListOf<ChargeProduct>()
        val menuItems = document.getElementsByTagName("MenuItem")
        for (i in 0 until menuItems.length) {
            val item = menuItems.item(i) as Element
            when (item.getAttribute("class")) {
                "normal_charge" -> parseKind(item, ChargeKind.VOUCHER, voucherOperators, products)
                "topup_charge" -> parseKind(item, ChargeKind.TOPUP, topUpOperators, products)
            }
        }
        return SadadChargeListSnapshot(voucherOperators, topUpOperators, products)
    }

    private fun parseKind(
        root: Element,
        kind: ChargeKind,
        operators: MutableList<ChargeOperator>,
        products: MutableList<ChargeProduct>,
    ) {
        childMenuItems(root).forEach { operatorNode ->
            if (operatorNode.getAttribute("class") != "operator") return@forEach
            if (!isEnabled(operatorNode)) return@forEach
            val collected = mutableListOf<ChargeProduct>()
            childMenuItems(operatorNode).forEach { child ->
                collectProducts(child, kind, groupLabel = "", collected)
            }
            val providerId = collected.firstOrNull()?.providerId.orEmpty()
            if (providerId.isBlank() || collected.isEmpty()) return@forEach
            operators += ChargeOperator(
                providerId = providerId,
                nameFa = operatorNode.getAttribute("fn"),
                nameEn = operatorNode.getAttribute("en"),
                ussdChargeCommand = operatorNode.getAttribute("usssdChargeCommand")
                    .ifBlank { operatorNode.getAttribute("ussdChargeCommand") },
                ussdGetSimCharge = operatorNode.getAttribute("ussdGetSimCharge"),
                taxPercent = operatorNode.getAttribute("taxPercent").toIntOrNull() ?: 0,
                minChargeAmount = operatorNode.getAttribute("minChargeAmount").toLongOrNull(),
            )
            products += collected
        }
    }

    private fun collectProducts(
        node: Element,
        kind: ChargeKind,
        groupLabel: String,
        out: MutableList<ChargeProduct>,
    ) {
        if (!isEnabled(node)) return
        val descriptor = firstChildElement(node, "ServiceDescriptor")
        val nested = childMenuItems(node)
        if (descriptor != null) {
            val amountRaw = descriptor.getAttribute("serviceAmount")
            out += ChargeProduct(
                id = descriptor.getAttribute("id"),
                kind = kind,
                providerId = descriptor.getAttribute("providerId"),
                amountRials = amountRaw.toLongOrNull(),
                categoryId = descriptor.getAttribute("categoryId")
                    .ifBlank { descriptor.getAttribute("topUpCategoryType") },
                serviceTypeCode = descriptor.getAttribute("serviceTypeCode"),
                loadUssd = descriptor.getAttribute("load"),
                labelFa = node.getAttribute("fn"),
                groupLabelFa = groupLabel,
                hasCount = when (descriptor.getAttribute("hasCount").lowercase()) {
                    "false" -> false
                    else -> true
                },
            )
            return
        }
        val nextGroup = node.getAttribute("fn")
        nested.forEach { child ->
            collectProducts(child, kind, groupLabel = nextGroup, out)
        }
    }

    private fun isEnabled(element: Element): Boolean =
        element.getAttribute("enable").lowercase() != "false"

    private fun childMenuItems(parent: Element): List<Element> {
        val result = mutableListOf<Element>()
        val nodes = parent.childNodes
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node is Element && node.tagName == "MenuItem") result += node
        }
        return result
    }

    private fun firstChildElement(parent: Element, tag: String): Element? {
        val nodes = parent.childNodes
        for (i in 0 until nodes.length) {
            val node = nodes.item(i)
            if (node is Element && node.tagName == tag) return node
        }
        return null
    }
}
