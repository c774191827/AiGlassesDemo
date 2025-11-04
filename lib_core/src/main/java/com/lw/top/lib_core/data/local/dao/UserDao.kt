package com.lw.top.lib_core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.lw.top.lib_core.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsersDao : BaseDao<UserEntity> {

    @Query("SELECT * FROM users WHERE userId = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :id")
    fun getUserByIdFlow(id: String): Flow<UserEntity?>

    @Query("UPDATE users SET nickname = :nickname WHERE userId = :userId")
    suspend fun updateNickname(userId: String, nickname: String)

    @Query("UPDATE users SET avatar = :avatar WHERE userId = :userId")
    suspend fun updateAvatar(userId: String, avatar: String)

    @Query("SELECT * FROM users ORDER BY loginTime DESC")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("UPDATE users SET isCurrentUser = 0")
    suspend fun clearAllCurrentUserFlags()

    @Query("UPDATE users SET isCurrentUser = 1 WHERE userId = :userId")
    suspend fun setCurrentUserFlag(userId: String)

    @Transaction
    suspend fun switchCurrentUser(userId: String) {
        clearAllCurrentUserFlags()
        setCurrentUserFlag(userId)
    }

    @Transaction
    suspend fun addNewUserAndSetAsCurrent(newUser: UserEntity) {
        insert(newUser)
        switchCurrentUser(newUser.userId)
    }


    @Query(
        """
        SELECT * FROM users 
        WHERE (:nickname IS NULL OR nickname LIKE '%' || :nickname || '%') 
        AND (:minAge IS NULL OR age >= :minAge) 
        AND (:status IS NULL OR status = :status)
        ORDER BY createTime DESC
    """
    )
    suspend fun queryUsers(
        nickname: String? = null,
        minAge: Int? = null,
        status: String? = null
    ): List<UserEntity>
}