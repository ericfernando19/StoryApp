package com.dicoding.storyapp.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.dicoding.storyapp.R

class CustomPasswordEditText : AppCompatEditText {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        // Menambahkan TextWatcher untuk validasi panjang password
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.length < 8) {
                    // Menampilkan error langsung jika panjang password kurang dari 8 karakter
                    error = context.getString(R.string.error_password_too_short) // Pesan error
                } else {
                    // Hapus pesan error jika password valid
                    error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
