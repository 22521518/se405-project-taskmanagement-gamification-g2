package com.example.se405.android.core.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.se405.android.R

val Montserrat = FontFamily(
    Font(R.font.montserrat_light, FontWeight.Light),
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold)
)

object AppText {

    // ===== Display =====
    val DisplayBold = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = Montserrat
    )

    val DisplaySemiBold = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = Montserrat
    )

    val DisplayRegular = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = Montserrat
    )

    val DisplayLight = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Light,
        fontFamily = Montserrat
    )


    // ===== Head =====
    val HeadBold = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = Montserrat
    )

    val HeadSemiBold = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = Montserrat
    )

    val HeadRegular = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = Montserrat
    )

    val HeadLight = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Light,
        fontFamily = Montserrat
    )


    // ===== Body =====
    val BodyBold = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = Montserrat
    )

    val BodySemiBold = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = Montserrat
    )

    val BodyRegular = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = Montserrat
    )

    val BodyLight = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Light,
        fontFamily = Montserrat
    )


    // ===== Body Small =====
    val Body2Bold = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = Montserrat
    )

    val Body2SemiBold = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = Montserrat
    )

    val Body2Regular = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = Montserrat
    )

    val Body2Light = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        fontFamily = Montserrat
    )


    // ===== Caption =====
    val CaptionBold = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = Montserrat
    )

    val CaptionSemiBold = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = Montserrat
    )

    val CaptionRegular = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = Montserrat
    )

    val CaptionLight = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Light,
        fontFamily = Montserrat
    )
}


data class TypographyItem(
    val name: String,
    val style: TextStyle
)

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    showSystemUi = true
)
@Composable
fun TypographyPreview() {
    MaterialTheme {
        Surface {
            val typographyList = listOf(

                // Display
                TypographyItem("DisplayBold", AppText.DisplayBold),
                TypographyItem("DisplaySemiBold", AppText.DisplaySemiBold),
                TypographyItem("DisplayRegular", AppText.DisplayRegular),
                TypographyItem("DisplayLight", AppText.DisplayLight),

                // Head
                TypographyItem("HeadBold", AppText.HeadBold),
                TypographyItem("HeadSemiBold", AppText.HeadSemiBold),
                TypographyItem("HeadRegular", AppText.HeadRegular),
                TypographyItem("HeadLight", AppText.HeadLight),

                // Body
                TypographyItem("BodyBold", AppText.BodyBold),
                TypographyItem("BodySemiBold", AppText.BodySemiBold),
                TypographyItem("BodyRegular", AppText.BodyRegular),
                TypographyItem("BodyLight", AppText.BodyLight),

                // Body2
                TypographyItem("Body2Bold", AppText.Body2Bold),
                TypographyItem("Body2SemiBold", AppText.Body2SemiBold),
                TypographyItem("Body2Regular", AppText.Body2Regular),
                TypographyItem("Body2Light", AppText.Body2Light),

                // Caption
                TypographyItem("CaptionBold", AppText.CaptionBold),
                TypographyItem("CaptionSemiBold", AppText.CaptionSemiBold),
                TypographyItem("CaptionRegular", AppText.CaptionRegular),
                TypographyItem("CaptionLight", AppText.CaptionLight),
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(typographyList) { item ->
                    Column {
                        Text(
                            text = item.name,
                            style = item.style,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}