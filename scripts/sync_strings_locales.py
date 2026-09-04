#!/usr/bin/env python3
"""Sync strings.xml across fa-rAF, ps-rAF, and fa-rIR locales for all modules."""
import re
import xml.etree.ElementTree as ET
from pathlib import Path

PROJECT = Path(__file__).resolve().parent.parent
LOCALES = ["values-fa-rAF", "values-ps-rAF", "values-fa-rIR"]

# Pashto translations keyed by string name (used when no locale file entry exists)
PS_BY_NAME: dict[str, str] = {
    # menu
    "menu_app_name": "همراه پی",
    "menu_terminal_label": "د ټرمینال شمیره:",
    "menu_purchase": "پیرود",
    "menu_report": "راپورونه",
    "menu_balance": "د موجودي پوښتنه",
    "menu_bill": "بل تادیه",
    "menu_topup": "موبایل چارج",
    "menu_transfer": "پیسې لیږد",
    "menu_cash_deposit": "نغدي زیرمه",
    "menu_cash_out": "نغدي ایستل",
    "menu_settings": "تنظیمات",
    "menu_promo_title": "د پذیرونکو لپاره ځانګړی ډالۍ",
    "menu_promo_amount": "10,000 AFN",
    "menu_qr_content_description": "د ټرمینال QR کوډ",
    # purchase
    "purchase_title": "پیرود",
    "transaction_info": "د معاملې معلومات",
    "plz_enter_transaction_info": "مهرباني وکړئ د معاملې معلومات ولیکئ",
    "amount_currency": "مبلغ (AFN)",
    "enter_amount": "مبلغ ولیکئ",
    "show_qr_code": "QR کوډ وښایاست",
    "pla_enter_amount": "مهرباني وکړئ مبلغ ولیکئ",
    "invalid_amount_entered": "داخل شوی مبلغ ناسم دی",
    "purchase_was_successful": "پیرود په بریالیتوب سره ترسره شو.",
    "purchase_failed": "پیرود ناکام شو.",
    # cash_out / cash_deposit shared
    "cash_out_success": "نغدي ایستل په بریالیتوب سره ترسره شو.",
    "cash_out_failed": "نغدي ایستل ناکام شو.",
    "cash_deposit_title": "نغدي زیرمه",
    "cash_deposit_success": "نغدي زیرمه په بریالیتوب سره ترسره شوه.",
    "cash_deposit_failed": "نغدي زیرمه ناکامه شوه.",
    # bill
    "bill_inquiry_result_title": "د بل پوښتنې پایله",
    "bill_label_bill_id": "د بل پیژندنه",
    "bill_label_debt_amount": "د پور مبلغ",
    "bill_label_tax": "مالیه",
    "bill_label_payment_deadline": "د تادیې وخت",
    "bill_label_payable_amount": "د تادیې وړ مبلغ",
    # topup
    "topup_title": "چارج پیرود",
    "topup_mobile_label": "د ترلاسه کوونکي موبایل شمیره",
    "topup_mobile_placeholder": "د موبایل شمیره ولیکئ",
    "topup_amount_label": "د چارج مبلغ (AFN)",
    "topup_amount_placeholder": "مبلغ ولیکئ",
    "topup_confirm": "تایید او دوام",
    # card_to_card
    "card_to_card_title": "پیسې لیږد",
    "card_to_card_transfer_to": "لیږد ته:",
    "card_to_card_amount_label": "د لیږد مبلغ:",
    "card_to_card_confirm": "د پیسو لیږد تایید",
    "card_to_card_card_label": "د کارت شمیره",
    "card_to_card_card_placeholder": "د منزل کارت شمیره ولیکئ",
    "card_to_card_amount_field_label": "مبلغ (AFN)",
    "card_to_card_amount_placeholder": "مبلغ ولیکئ",
    "card_to_card_continue": "تایید او دوام",
    # balance module
    "customer_receipt": "د پیرودونکي رسید",
    "merchant_receipt": "د پذیرونکي رسید",
    "duplicate_receipt": "بیا رسید",
    "sum_of_all_transactions": "د ټولو معاملو مجموعه",
    "amount_with_currency": "%1$s ریال",
    "trace__": "د تعقیب شمیره/",
    "trace_rrn": "د تعقیب/مرجع شمیره",
    "balance_": "موجودي",
    "balance_transaction_fee": "د موجودي عملیات فیس",
    "withdrawable_account_balance": "د ایستلو وړ حساب بیلانس",
    "voucher_serial": "د چارج سریال",
    "voucher_pin": "د چارج پټنوم",
    "customer_signature": "د پیرودونکي لاسلیک",
    "success_operation": "بریالی عملیات",
    "unsuccess_transaction": "ناکامه معامله",
    "card_number_title": "د کارت شمیره",
    # core/ui
    "label_back": "بیرته",
    "balance_title": "د موجودي اعلان",
    "label_terminal_merchant": "ټرمینل / پذیرونکی",
    "balance_home": "کور پاڼه",
    "balance_print_receipt": "رسید چاپ",
    # settings
    "settings_close": "تړل",
    "settings_theme_sheet_title": "د اپلیکیشن تم انتخاب",
    "settings_theme_dark": "تیاره",
    "settings_theme_light": "روښانه",
    "settings_theme_dark_icon": "تیاره تم",
    "settings_theme_light_icon": "روښانه تم",
    "settings_font_sheet_title": "د اپلیکیشن فونت انتخاب",
    "settings_font_yekan_bakh": "Yekan_Bakh",
    "settings_font_sans_serif": "Sans-Serif",
    "settings_language_sheet_title": "د اپلیکیشن ژبه انتخاب",
    "settings_language_persian_dari": "فارسی-دری",
    "settings_language_persian_pashto": "فارسی-پشتو",
    "settings_language_other": "نور",
    "settings_role_sheet_title": "رول انتخاب",
    "settings_role_support": "د پشتیبان تنظیمات",
    "settings_role_merchant": "د پذیرونکي تنظیمات",
    "settings_role_illustration": "رول انتخاب",
    "settings_merchant_title": "د پذیرونکي تنظیمات",
    "settings_change_password_hint": "اوسنی او نوی پټنوم ولیکئ",
    "settings_change_password_current": "اوسنی پټنوم",
    "settings_change_password_new": "نوی پټنوم",
    "settings_change_password_confirm": "نوی پټنوم تکرار",
    "settings_change_password_confirm_button": "د پټنوم بدلون تایید",
    "settings_change_password_error_current": "مهرباني وکړئ اوسنی پټنوم ولیکئ",
    "settings_change_password_error_new": "مهرباني وکړئ نوی پټنوم ولیکئ",
    "settings_change_password_error_confirm": "مهرباني وکړئ نوی پټنوم تکرار ولیکئ",
    "settings_change_password_error_mismatch": "نوی پټنوم او تکرار یو شان نه دي",
    "settings_device_section_title": "تنظیمات",
    "settings_print_receipt": "رسید چاپ",
    "settings_set_shift": "شفټ ټاکل",
    "settings_change_management_password": "پټنوم بدلول",
    "settings_select_font": "فونت انتخاب",
    "settings_select_language": "ژبه انتخاب",
    "settings_app_theme": "اپلیکیشن تم",
    "settings_support_title": "د پشتیبان تنظیمات",
    "settings_section_title": "تنظیمات",
    "settings_support_internet": "انټرنیټ اتصال",
    "settings_support_connected": "Connect",
    "settings_support_disconnected": "Disconnect",
    "settings_support_server_ip": "IP معلومات",
    "settings_support_server_port": "PORT معلومات",
    "settings_support_terminal": "د ټرمینال معلومات",
    "settings_support_configuration": "ترتیب",
    "settings_support_ip_sheet_title": "IP بدلول",
    "settings_support_port_sheet_title": "PORT بدلول",
    "settings_support_ip_hint": "د سرور IP پته",
    "settings_support_port_hint": "د سرور پورت شمیره",
    "settings_support_confirm": "تایید",
    "settings_support_ip_error_empty": "مهرباني وکړئ IP پته ولیکئ",
    "settings_support_ip_error_invalid": "IP پته ناسمه ده",
    "settings_support_port_error_empty": "مهرباني وکړئ پورت شمیره ولیکئ",
    "settings_support_port_error_invalid": "پورت شمیره باید له ۱ څخه تر ۶۵۵۳۵ پورې وي",
    "settings_support_keypad_backspace": "حذف",
    "settings_support_keypad_clear": "پاک",
    # report
    "report_filters_title": "د راپور فیلترونه",
    "report_filters_close": "تړل",
    "report_filters_tracking_number": "د تعقیب شمیره",
    "report_filters_reference_number": "مرجع شمیره",
    "report_filters_transaction_status": "د معاملې حالت",
    "report_filters_transaction_date": "د معاملې نیټه",
    "report_filters_transaction_time": "د معاملې وخت",
    "report_filters_apply": "فیلترونه پلي کړئ",
    "report_filters_confirm": "تایید",
    "report_filters_cancel": "لغوه",
    "report_status_all": "ټول",
    "report_status_success": "بریالی",
    "report_status_failed": "ناکام",
    "report_status_pending": "په انتظار کې",
    "report_title": "راپورونه",
    "report_back": "بیرته",
    "report_total_today": "د نن ټولې معاملې",
    "report_successful_count": "بریالی معامله",
    "report_aggregate": "مجموعي راپور",
    "report_transaction_details": "د معاملو جزئیات",
    "report_last_transaction": "وروستۍ معامله",
    "report_empty": "په دې فیلترونو کې هیڅ معامله ونه موندل شوه",
    "report_unknown_merchant": "نامعلوم پذیرونکی",
    "report_amount_label": "مبلغ",
    "report_transaction_type": "د معاملې ډول",
    "report_response_code": "د ځواب کوډ",
    "report_response_message": "د ځواب پیغام",
    "report_password_hint": "پټنوم ناسم دی",
    # splash
    "content_desc_splash": "POS Terminal",
    "app_name": "PayAfghanestan",
}

# Persian phrase -> Pashto for fallback translation of unknown keys
PS_PHRASES: list[tuple[str, str]] = [
    ("لطفا ", "مهرباني وکړئ "),
    ("لطفاً ", "مهرباني وکړئ "),
    (" را وارد کنید", " ولیکئ"),
    (" را وارد کنید ", " ولیکئ "),
    ("شماره ترمینال", "د ټرمینال شمیره"),
    ("شماره پایانه", "د ټرمینال شمیره"),
    ("تراکنش", "معامله"),
    ("موفق", "بریالی"),
    ("ناموفق", "ناکام"),
    ("بازگشت", "بیرته"),
    ("تایید", "تایید"),
    ("انصراف", "لغوه"),
    ("مبلغ", "مبلغ"),
    ("خرید", "پیرود"),
    ("گزارشات", "راپورونه"),
    ("تنظیمات", "تنظیمات"),
    ("چاپ رسید", "رسید چاپ"),
    ("صفحه اصلی", "کور پاڼه"),
    ("پذیرنده", "پذیرونکی"),
    ("پایانه", "ټرمینل"),
    ("استعلام موجودی", "د موجودي پوښتنه"),
    ("پرداخت قبض", "بل تادیه"),
    ("شارژ موبایل", "موبایل چارج"),
    ("انتقال وجه", "پیسې لیږد"),
    ("واریز نقدی", "نغدي زیرمه"),
    ("برداشت نقدی", "نغدي ایستل"),
    ("بستن", "تړل"),
    ("همه", "ټول"),
    ("در انتظار", "په انتظار کې"),
    ("اطلاعات", "معلومات"),
    ("رمز اشتباه است", "پټنوم ناسم دی"),
    ("شماره کارت", "د کارت شمیره"),
    ("نوع تراکنش", "د معاملې ډول"),
    ("شماره پیگیری", "د تعقیب شمیره"),
]


def parse_strings(path: Path) -> dict[str, str]:
    if not path.exists():
        return {}
    tree = ET.parse(path)
    return {el.get("name"): (el.text or "") for el in tree.findall("string")}


def escape_xml(text: str) -> str:
    return (
        text.replace("&", "&amp")
        .replace("<", "&lt")
        .replace(">", "&gt")
        .replace('"', "&quot")
        .replace("'", "\\'")
    )


def to_fa_ir(text: str, name: str) -> str:
    """Iranian Persian variants."""
    t = text.replace("لطفا ", "لطفاً ")
    if name == "validation_mobile_prefix" or name == "error_mobile_start_07":
        t = t.replace("۰۷", "۰۹")
    if name == "menu_report":
        t = "گزارش‌ها"
    return t


def to_pashto(text: str, name: str) -> str:
    if name in PS_BY_NAME:
        return PS_BY_NAME[name]
    result = text
    for fa, ps in PS_PHRASES:
        result = result.replace(fa, ps)
    return result


def write_strings(path: Path, strings: dict[str, str], localized: bool) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    lines: list[str] = []
    if localized:
        lines.append('<?xml version="1.0" encoding="utf-8"?>')
    lines.append("<resources>")
    for name in sorted(strings.keys()):
        value = escape_xml(strings[name])
        lines.append(f'    <string name="{name}">{value}</string>')
    lines.append("</resources>")
    lines.append("")
    path.write_text("\n".join(lines), encoding="utf-8")


def sync_module(base_file: Path) -> list[str]:
    reports: list[str] = []
    base = parse_strings(base_file)
    if not base:
        return reports

    module = base_file.parent.parent.name  # res/values -> res
    rel = base_file.relative_to(PROJECT)

    for locale in LOCALES:
        locale_dir = base_file.parent.parent / locale
        locale_file = locale_dir / "strings.xml"
        existing = parse_strings(locale_file)

        merged: dict[str, str] = {}
        for name, value in base.items():
            if locale == "values-fa-rAF":
                merged[name] = existing.get(name, value)
            elif locale == "values-fa-rIR":
                merged[name] = existing.get(name, to_fa_ir(value, name))
            else:  # ps-rAF
                merged[name] = existing.get(name) or to_pashto(value, name)

        missing_before = set(base.keys()) - set(existing.keys())
        if missing_before or not locale_file.exists():
            write_strings(locale_file, merged, localized=True)
            if missing_before:
                reports.append(
                    f"  {rel.parent.parent.parent.name}: {locale} +{len(missing_before)} keys"
                )
            elif not existing:
                reports.append(
                    f"  {rel.parent.parent.parent.name}: {locale} created ({len(merged)} keys)"
                )

    return reports


def main() -> None:
    base_files = sorted(PROJECT.glob("**/src/main/res/values/strings.xml"))
    all_reports: list[str] = []
    for bf in base_files:
        all_reports.extend(sync_module(bf))

    print(f"Processed {len(base_files)} base strings.xml files")
    for r in all_reports:
        print(r)
    if not all_reports:
        print("All locale files already complete.")


if __name__ == "__main__":
    main()
