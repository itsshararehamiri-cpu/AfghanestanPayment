pluginManagement {
    repositories {
        maven("https://maven.myket.ir")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://maven.myket.ir")
    }
}

rootProject.name = "AfghanestanPayment"

include(":app")

// Core
include(":core:common")
include(":core:database")
include(":core:ui")

// Device
include(":device:core")
include(":device:KNine")

// Features
include(":feature:purchase")
include(":feature:balance")
include(":feature:settings")
include(":feature:bill")
include(":feature:common")
include(":feature:topup")
include(":feature:support")
include(":feature:report")
include(":feature:splash")
include(":feature:card_to_card")
include(":feature:wallet_to_wallet")
include(":feature:menu")
include(":feature:cash_deposit")
include(":feature:cash_out")

// PSP
include(":psp:api")
include(":psp:hp")
include(":psp:bp")
//include(":psp:fanava")

// Transaction
include(":transaction:api")
include(":transaction:engine")

// Connection
include(":connection:core")
include(":connection:iso")
//include(":feature:voucher")
include(":feature:voucher")
include(":psp:sadad")
