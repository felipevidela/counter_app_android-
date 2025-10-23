package com.example.counter_app.data

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getUser(username: String): User? {
        return userDao.getUser(username)
    }
}
