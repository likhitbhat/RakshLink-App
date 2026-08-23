package com.rakshalink.domain.model

enum class UserRole {
    WEARER,
    GUARDIAN;

    companion object {
        fun fromString(role: String?): UserRole {
            return when (role?.lowercase()) {
                "guardian" -> GUARDIAN
                else -> WEARER
            }
        }
    }
}
