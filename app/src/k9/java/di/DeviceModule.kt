//package di
//import android.content.Context
//import com.danesh.core.Device
//import com.danesh.knine.K9
//import dagger.Binds
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
////
//@Module
//@InstallIn(SingletonComponent::class)
//abstract class DeviceBindingModule {
//
//    @Binds
//    @Singleton
//    abstract fun bindDevice(k9: K9): Device
//}
//
////
//@Module
//@InstallIn(SingletonComponent::class)
//object DeviceModule {
//
//    @Provides
//    @Singleton
//    fun provideDevice(
//
//    ): Device = K9(ApplicationContext::class.java as Context)
//}
