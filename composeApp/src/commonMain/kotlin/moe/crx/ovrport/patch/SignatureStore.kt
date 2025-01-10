package moe.crx.ovrport.patch

import com.android.apksig.ApkSigner.SignerConfig
import com.android.apksig.KeyConfig
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.Calendar
import java.util.Date
import java.util.UUID

object SignatureStore {
    private const val KS_PASSWORD = "password"
    private const val KS_ALIAS = "key"

    private fun readSignature(file: File): SignerConfig {
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())

        FileInputStream(file).use { stream ->
            ks.load(stream, KS_PASSWORD.toCharArray())
        }

        val alias = ks.aliases().nextElement()

        val key = ks.getKey(alias, KS_PASSWORD.toCharArray()) as PrivateKey
        val cert = ks.getCertificate(alias) as X509Certificate

        return SignerConfig.Builder(cert.issuerX500Principal.name, KeyConfig.Jca(key), listOf(cert)).build()
    }

    private fun generateCertificate(keyPair: KeyPair): Certificate {
        val now = System.currentTimeMillis()
        val startDate = Date(now)
        val expireDate = Calendar.getInstance().run {
            time = startDate
            add(Calendar.YEAR, 25)
            time
        }
        val dnName = UUID
            .randomUUID()
            .toString()
            .replace("-", "")
            .prependIndent("CN=")
            .let { X500Name(it) }
        val serial = BigInteger(64, SecureRandom())

        val signer = JcaContentSignerBuilder("SHA256withRSA").setProvider(BouncyCastleProvider()).build(keyPair.private)

        return JcaX509v3CertificateBuilder(dnName, serial, startDate, expireDate, dnName, keyPair.public).build(signer).let {
            JcaX509CertificateConverter().setProvider(BouncyCastleProvider()).getCertificate(it)
        }
    }

    private fun createSignature(file: File) {
        val ks = KeyStore.getInstance(KeyStore.getDefaultType())
        ks.load(null, KS_PASSWORD.toCharArray())

        val keyPair = KeyPairGenerator
            .getInstance("RSA", BouncyCastleProvider())
            .apply { initialize(2048) }
            .generateKeyPair()
        val cert = generateCertificate(keyPair)

        ks.setKeyEntry(KS_ALIAS, keyPair.private, KS_PASSWORD.toCharArray(), arrayOf(cert))

        FileOutputStream(file).use { stream ->
            ks.store(stream, KS_PASSWORD.toCharArray())
        }
    }

    fun getSignature(file: File): SignerConfig {
        if (!file.exists()) {
            createSignature(file)
        }

        return readSignature(file)
    }
}