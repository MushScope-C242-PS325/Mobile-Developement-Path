package com.mushscope.view.customview

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import com.google.android.material.textfield.TextInputEditText
import com.mushscope.R

class EmailEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TextInputEditText(context, attrs) {
    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateEmail(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })
    }

    private fun validateEmail(email: String) {
        when {
            email.isEmpty() -> {
                setError(context.getString(R.string.email_cannot_empty), null)
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                setError(context.getString(R.string.unvalid_email_format), null)
            }
            else -> {
                error = null
            }
        }
    }
}