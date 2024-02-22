@file:Suppress("RedundantNullableReturnType")

package academy.kt

class User(val id: String)

class UserService(
    private val tokenService: TokenService
) {

    @Measured
    fun findUser(id: Int): User {
        tokenService.getToken()
        Thread.sleep(1000)
        return User("$id")
    }

    @Measured
    fun findUsers(): User {
        Thread.sleep(1000)
        return User("")
    }
}

class TokenService {

    @Measured
    fun getToken(): String {
        Thread.sleep(1000)
        return "ABCD"
    }
}

fun main(args: Array<String>) {
    val tokenService = TokenService()
    val userService = UserService(tokenService)
    val measuredService = MeasuredUserService(tokenService)
    val user = measuredService.findUser(12) 
    // findUser from UserService took 200Xms
    val user2 = measuredService.findUsers() 
    // findUser from UserService took 100Xms

    val measuredTokenService = MeasuredTokenService(tokenService)
    val token = measuredTokenService.getToken() 
    // getToken from TokenService took 100Xms
}
