package ru.netology.neworkapplication.util

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import ru.netology.neworkapplication.R
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T>(private val context: Context) : MutableLiveData<T>() {
    private var pending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T?>) {
        require(!hasActiveObservers()) {
            error(context.getString(R.string.multiple_observers_error))
        }

        super.observe(owner) {
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(it)
            }
        }
    }

    @MainThread
    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }
}

