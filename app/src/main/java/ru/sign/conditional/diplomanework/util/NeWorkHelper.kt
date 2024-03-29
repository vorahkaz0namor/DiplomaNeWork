package ru.sign.conditional.diplomanework.util

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.textfield.TextInputLayout
import okhttp3.internal.http.HTTP_FORBIDDEN
import okhttp3.internal.http.HTTP_NOT_FOUND
import okhttp3.internal.http.HTTP_NO_CONTENT
import okhttp3.internal.http.HTTP_UNAUTHORIZED
import retrofit2.HttpException
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.dto.*
import java.math.BigDecimal
import java.net.ConnectException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.ceil

object NeWorkHelper {
    /** Russian language code in ISO 639-1 standard */
    private val russianLanguageCode = Locale("ru").language
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
                HTTP_UNAUTHORIZED -> "Unauthorized"
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
    private val parseDateTimeThroughInstant = { time: CharSequence ->
        OffsetDateTime.parse(time)
    }
    private var roundingRadius = 0

    private fun manyEndsWith(string: String, vararg ends: String): Boolean {
        ends.map {
            if (string.endsWith(it))
                return true
        }
        return false
    }

    fun setRoundingRadius(pixels: Float) {
        roundingRadius = ceil(pixels).toInt()
    }

    fun datetimeWithOffset(time: String): NeWorkDatetime =
        parseDateTimeThroughInstant(time).let {
            NeWorkDatetime(
                year = it.year.toString(),
                month = it.monthValue.toString(),
                day = it.dayOfMonth.toString(),
                hour = it.hour.toString(),
                minute = it.minute.toString()
            )
        }

    fun publishedCustomRepresentation(
        time: CharSequence,
        pattern: String = "dd MMMM yyyy, HH:mm"
    ): String = parseDateTimeThroughInstant(time)
        .format(DateTimeFormatter.ofPattern(pattern))

    fun datetimeCustomRepresentation(time: CharSequence): String =
        publishedCustomRepresentation(
            time = time,
            pattern = "E, dd MMMM, yyyy, HH:mm"
        )

    fun jobDatetimeCustomRepresentation(time: CharSequence): String =
        publishedCustomRepresentation(
            time = time,
            pattern = "MMM yyyy"
        )

    fun View.setFeedItemMenu(
        actionEdit: () -> Unit,
        actionRemove: () -> Unit
    ) {
        PopupMenu(context, this).apply {
            inflate(R.menu.feed_item_options)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit_feed_item -> {
                        actionEdit()
                        true
                    }
                    R.id.remove_feed_item -> {
                        actionRemove()
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

    fun TextInputLayout.setDatetimeToView(text: CharSequence?) {
        this.editText?.let { content ->
            if (!content.text.contentEquals(text))
                content.setText(text)
        }
    }

    fun ImageView.loadImage(
        url: String,
        type: String = AttachmentType.AVATAR.name,
        @DrawableRes placeholder: Int = R.drawable.ic_loading_32,
        @DrawableRes fallback: Int = R.drawable.ic_error_32,
        timeout: Int = 5_000
    ) {
        Glide.with(this)
            .load(url)
            .timeout(timeout)
            .placeholder(placeholder)
            .error(fallback)
            .let {
                if (type == AttachmentType.AVATAR.name)
                    it.circleCrop()
                else
                    it.transform(RoundedCorners(roundingRadius))
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
            }
            .into(this)
    }

    fun View.attendeesEndings(count: Int): String {
        var result =
            context.getString(
                R.string.participants_count,
                count.toString()
            )
        if (Locale.getDefault().language == russianLanguageCode) {
            val string = count.toString()
            result = result.plus(
                when {
                    manyEndsWith(
                        string = string,
                        ends = arrayOf("2", "3", "4")
                    ) && !manyEndsWith(
                        string = string,
                        ends = arrayOf("12", "13", "14")
                    ) -> "а"
                    manyEndsWith(
                        string = string,
                        ends = arrayOf("0", "11")
                    ) || !string.endsWith("1") -> "ов"
                    else -> ""
                }
            )
        }
        return result
    }

    fun itemsCount(count: Int): String =
        when {
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

    fun View.jobDurationCount(start: String, finish: String?): String =
        (finish?.let {
            parseDateTimeThroughInstant(it)
        } ?: Instant.now().atOffset(OffsetDateTime.now().offset))
            .let { mappedFinish ->
                val mappedStart = parseDateTimeThroughInstant(start)
                var ys = mappedFinish.year - mappedStart.year
                var mos = (mappedFinish.monthValue - mappedStart.monthValue)
                    .plus(if (ys == 0) 1 else 13)
                if (mos >= 12)
                    mos -= 12
                else
                    ys -= 1
                buildString {
                    if (ys > 0)
                        append(
                            context.getString(
                                R.string.work_years_count,
                                ys.toString()
                            )
                        )
                    if (mos > 0)
                        append(
                            context.getString(
                                R.string.work_months_count,
                                mos.toString()
                            )
                        )
                }
            }

    fun getEventNameFromContent(content: String) =
        content.substringBefore(".")

    fun customLog(action: String, e: Exception) {
        Log.d(action, "CAUGHT EXCEPTION => $e\n" +
                "DESCRIPTION => ${overview(exceptionCheck(e))}")
    }
}