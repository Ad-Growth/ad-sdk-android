package com.adgrowth.adserver.entities

import org.json.JSONObject

class ClientProfile {
    var gender = Gender.ALL
    var clientAddress = ClientAddress(JSONObject())
    var age = 0
    var minAge = 0
    var maxAge = 0
    var interests = ArrayList<String>()
    fun addInterest(interest: String) {
        interests.add(interest)
    }

    fun removeInterest(interest: String) {
        interests.removeIf { s: String -> s == interest }
    }

    enum class Gender {
        ALL, MALE, FEMALE;
    }
}
