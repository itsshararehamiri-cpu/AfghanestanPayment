package com.danesh.purchase.data

class PurchaseTransactionHandler {
}
/*
 Flow واقعی اجرای Purchase
این قسمت مهم است.

مرحله 1
UI:

text
feature-purchase
مرحله 2
UseCase:

kotlin
engine.execute(
    PurchaseHandler(...)
)
مرحله 3
Engine:

text
flush queue
save reversal
save report
مرحله 4
PurchaseHandler:

text
build ISO message
مرحله 5
PSP plugin:

text
apply packager
map fields
مرحله 6
Communication:

text
send/receive
مرحله 7
PSP parser:

text
parse response
مرحله 8
Engine:

text
update report
resolve reversal
مرحله 9
Feature:

text
show receipt
 */