package org.kotlink.framework.ui

interface FormErrors {
    fun isFieldValid(field: String): Boolean
    fun getForField(field: String): List<String>
}