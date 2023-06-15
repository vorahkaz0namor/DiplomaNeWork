package ru.sign.conditional.diplomanework.util

import androidx.lifecycle.*

class SingleLiveEvent<T>(value: T) : MutableLiveData<T>(value) {
    private var pending = false

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        require(!hasActiveObservers()) {
            error("Multiple observers registered but only one will be notified of changes")
        }
        super.observe(owner) {
            if (pending) {
                pending = false
                observer.onChanged(it)
            }
        }
    }

    override fun setValue(value: T?) {
        pending = true
        super.setValue(value)
    }
}