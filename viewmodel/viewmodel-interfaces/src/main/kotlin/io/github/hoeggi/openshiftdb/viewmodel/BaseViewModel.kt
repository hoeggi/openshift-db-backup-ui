package io.github.hoeggi.openshiftdb.viewmodel

import kotlinx.coroutines.CoroutineScope

interface ViewModelFactory {
    val ocViewModel: OcViewModel
    val postgresViewModel: PostgresViewModel
    val eventsViewModel: EventsViewModel
    val syslogViewModel: SyslogViewModel
}

interface BaseViewModel {
    val coroutineScope: CoroutineScope
}
