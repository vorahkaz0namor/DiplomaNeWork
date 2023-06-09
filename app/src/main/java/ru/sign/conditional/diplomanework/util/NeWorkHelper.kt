package ru.sign.conditional.diplomanework.util

import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import okhttp3.internal.http.HTTP_FORBIDDEN
import okhttp3.internal.http.HTTP_NOT_FOUND
import okhttp3.internal.http.HTTP_NO_CONTENT
import retrofit2.HttpException
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.dto.AttachmentType
import java.math.BigDecimal
import java.net.ConnectException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object NeWorkHelper {
    /** `520 Unknown Error` (non-standard HTTP code CloudFlare) */
    const val HTTP_UNKNOWN_ERROR = 520
    /** `444 Connection Failed` (thought up code) */
    private const val HTTP_CONNECTION_FAILED = 444
    /** HTTP code type check */
    val overview = { code: Int ->
        when (code) {
            in 200..299 -> if (code == HTTP_NO_CONTENT)
                "Body is null"
            else
                "Successful"
            in 400..499 -> when (code) {
                HTTP_CONNECTION_FAILED -> "Connection failed"
                HTTP_NOT_FOUND -> "Not found"
                HTTP_FORBIDDEN -> "Forbidden"
                else -> "Bad request"
            }
            in 500..599 -> if (code == HTTP_UNKNOWN_ERROR)
                "Unknown error"
            else
                "Internal server error"
            else -> "Continue..."
        }
    }
    /** Exception type check */
    val exceptionCheck = { e: Exception ->
        when (e) {
            is HttpException -> e.code()
            is ConnectException -> HTTP_CONNECTION_FAILED
            else -> HTTP_UNKNOWN_ERROR
        }
    }
    val timeCustomRepresentation = { time: CharSequence ->
            Instant.parse(time)
                .atOffset(OffsetDateTime.now().offset)
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm:ss"))
    }

    private fun ImageView.loadSelective(
        url: String = "",
        resourceId: Int = 0,
        type: String,
        @DrawableRes placeholder: Int = R.drawable.ic_loading_32,
        @DrawableRes fallback: Int = R.drawable.ic_error_32,
        timeout: Int = 8_000
    ) {
        Glide.with(this)
            .load(
                if (resourceId == 0)
                    url
                else
                    resourceId
            )
            .timeout(timeout)
            .placeholder(placeholder)
            .error(fallback)
            .apply {
                (if (type == AttachmentType.AVATAR.name)
                    circleCrop()
                else
                    dontTransform()
                ).into(this@loadSelective)
            }
    }

    fun ImageView.loadImage(
        url: String,
        type: String = AttachmentType.AVATAR.name
    ) {
        loadSelective(url = url, type = type)
    }

    fun ImageView.loadImage(
        resourceId: Int = 0
    ) {
        loadSelective(
            resourceId = resourceId,
            type = AttachmentType.AVATAR.name
        )
    }

    fun likesCount(count: Int): String {
        return when {
            count < 0 -> "0"
            count < 1_000 -> count.toString()
            else -> {
                val digitsMap = mapOf(
                    1 to "",
                    1_000 to "K",
                    1_000_000 to "M"
                )
                val divisor = digitsMap.keys.elementAt(
                    when {
                        count >= digitsMap.keys.elementAt(2) -> 2
                        count >= digitsMap.keys.elementAt(1) -> 1
                        else -> 0
                    }
                )
                "${BigDecimal(count).divide(
                    BigDecimal(divisor),
                    if (count % divisor == 0 ||
                            count % divisor < 100 ||
                            count / divisor >= 10)
                        0
                    else
                        1,
                    BigDecimal.ROUND_DOWN
                )}${digitsMap.getValue(divisor)}"
            }
        }
    }

    fun customLog(action: String, e: Exception) {
        Log.d(action, "CAUGHT EXCEPTION => $e\n" +
                "DESCRIPTION => ${overview(exceptionCheck(e))}")
    }
}