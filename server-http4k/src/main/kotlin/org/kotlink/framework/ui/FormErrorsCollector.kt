package org.kotlink.framework.ui

class FormErrorsCollector(
    private val treatAllErrorsAsPotential: Boolean = false
) : FormErrors {

    /*
       XXX "potential" errors don't actually mean that invalid data was submitted to the backend,
       but we need to add them to HTML for the client side validation to show them if what user is typing is invalid
     */
    private val potentialErrorsByField: MutableMap<String, MutableList<String>> = mutableMapOf()
    private val actualErrorsByField: MutableMap<String, MutableList<String>> = mutableMapOf()

    override fun isFieldValid(field: String): Boolean = actualErrorsByField[field].let { fieldErrors ->
        fieldErrors == null || fieldErrors.isEmpty()
    }

    override fun getForField(field: String): List<String> =
        (potentialErrorsByField[field] ?: emptyList()) + (actualErrorsByField[field] ?: emptyList())

    fun allFieldValid(): Boolean = actualErrorsByField.isEmpty()

    fun addForField(field: String, error: String, actualError: Boolean = true) {
        val errorsByField = if (actualError && !treatAllErrorsAsPotential) actualErrorsByField else potentialErrorsByField
        val fieldErrors = errorsByField.getOrPut(field) { mutableListOf() }
        fieldErrors += error
    }
}