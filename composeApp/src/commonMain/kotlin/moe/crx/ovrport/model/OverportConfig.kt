package moe.crx.ovrport.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OverportConfigLanguage(
    @SerialName("english_name")
    var englishName: String = "",
    @SerialName("native_name")
    var nativeName: String = "",
    @SerialName("tag")
    var tag: String = "",
)

@Serializable
data class OverportConfigAsset(
    @SerialName("asset_id")
    var assetId: Long = 0,
    @SerialName("file_path")
    var filePath: String,
    @SerialName("asset_type")
    var assetType: String = "store",
    @SerialName("language")
    var language: OverportConfigLanguage = OverportConfigLanguage(),
    @SerialName("download_status")
    var downloadStatus: String = "installed",
    @SerialName("iap_status")
    var iapStatus: String = "entitled",
    @SerialName("metadata")
    var metadata: String = "",
)

@Serializable
data class OverportConfigApplication(
    @SerialName("app_id")
    var appId: String,
    @SerialName("products")
    var products: List<String>,
    @SerialName("assets")
    var assets: List<OverportConfigAsset>,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class OverportConfigV1(
    @SerialName("version")
    @EncodeDefault
    var version: Int = 1,
    @SerialName("suggested_texture_width")
    var suggestedTextureWidth: Int = 0,
    @SerialName("suggested_texture_height")
    var suggestedTextureHeight: Int = 0,
    @SerialName("suggested_cpu_level")
    var suggestedCpuLevel: Int = 0,
    @SerialName("suggested_gpu_level")
    var suggestedGpuLevel: Int = 0,
    @SerialName("vibration_multiplier")
    var vibrationMultiplier: Double = 1.0,
    @SerialName("vibration_frequency_multiplier")
    var vibrationFrequencyMultiplier: Double = 1.0,
    @SerialName("database")
    var database: List<OverportConfigApplication>,
)