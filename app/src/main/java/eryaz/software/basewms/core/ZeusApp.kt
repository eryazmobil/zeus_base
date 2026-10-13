package eryaz.software.basewms.core

import eryaz.software.zeus.ZeusApplication
import timber.log.Timber

/**
 * Koin ve SessionManager ZeusApplication tarafından başlatılır.
 * Burada SADECE uygulamaya özel başlangıç işleri yapılır (Koin yazılamaz).
 */
class ZeusApp : ZeusApplication() {

    override fun onZeusCreate() {
        Timber.plant(Timber.DebugTree())
    }
}
