package com.danesh.afghanestanpayment.di
//
import android.content.Context
import com.danesh.core.Device
import com.danesh.knine.K9
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
//
//@Module
//@InstallIn(SingletonComponent::class)
//abstract class DeviceBindingModule {
//
//    @Binds
//    @Singleton
//    abstract fun bindDevice(
//        k9: K9
//    ): Device
//}
////@Module
////@InstallIn(SingletonComponent::class)
////object DeviceBindingModule {
////    @Binds
////    @Singleton
////    abstract fun bindDevice(
////        k9: K9
////    ): Device
//////    @Provides
//////    @Singleton
//////    fun provideK9Device(
//////        @ApplicationContext context: Context
//////    ): Device {
//////        return K9(context)
//////    }
//////    @Provides
//////    fun provideDevice(k9: K9): Device = k9
////}
//
//
//
//
//
////@Module
////@InstallIn(SingletonComponent::class)
////object DeviceModule {
////
//////    @Provides
//////    @Singleton
//////    fun providePrinter(config: AppRuntimeConfig): Printer {
//////        return when (config.activeDevice) {
//////            ActiveDevice.PAX -> PaxPrinter()
//////            ActiveDevice.UROVO -> UrovoPrinter()
//////        }
//////    }
//////
//////    @Provides
//////    @Singleton
//////    fun provideCardReader(config: AppRuntimeConfig): CardReader {
//////        return when (config.activeDevice) {
//////            ActiveDevice.PAX -> PaxCardReader()
//////            ActiveDevice.UROVO -> UrovoCardReader()
//////        }
//////    }
////
////    @Provides
////    @Singleton
////    fun provideDevice(): Device {
////        return K9()
////    }
////}
