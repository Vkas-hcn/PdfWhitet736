package com.two.smooth

import org.junit.Test

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
//    private val soName = "libpwsss.so"
//    private val progetName = "T736"
//    private val name64 = "instruction.pdf"
//    private val name32 = "example.pdf"

    // h5
    private val soName = "libnuttr.so"
    private val progetName = "T736"
    private val name64 = "instruction.doc"
    private val name32 = "example.doc"

    @Test
    fun addition_isCorrect() {
        val inputFile = "/Users/vkas/Desktop/soencode/$progetName/arm64-v8a/$soName"

        // 加密后文件路径 64
        val encryptedFile = "/Users/vkas/Desktop/soencode/$progetName/$name64"

        encrypt(File(inputFile).inputStream(), File(encryptedFile))

        val inputFile2 = "/Users/vkas/Desktop/soencode/$progetName/armeabi-v7a/$soName"
        // 加密后文件路径
        val encryptedFile2 = "/Users/vkas/Desktop/soencode/$progetName/$name32"
        encrypt(File(inputFile2).inputStream(), File(encryptedFile2))
    }

    private val ALGORITHM = "AES"
    private val SECRET_KEY = "WnVJ8ekmcEJN9jcq".toByteArray() // 16, 24, or 32 bytes

    // 加密
    fun encrypt(inputStream: InputStream, outputFile: File) {
        val key = SecretKeySpec(
            SECRET_KEY, ALGORITHM
        )
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val outputStream = FileOutputStream(outputFile)
        val inputBytes = inputStream.readBytes()
        val outputBytes = cipher.doFinal(inputBytes)
        outputStream.write(outputBytes)
        outputStream.close()
        inputStream.close()
    }

    // 解密
    fun decrypt(inputFile: InputStream, outputFile: File) {
        val key = SecretKeySpec(
            SECRET_KEY, ALGORITHM
        )
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val outputStream = FileOutputStream(outputFile)
        val inputBytes = inputFile.readBytes()
        val outputBytes = cipher.doFinal(inputBytes)
        outputStream.write(outputBytes)
        outputStream.close()
        inputFile.close()
    }


    private val pathBASE = "/Users/vkas/AndroidStudioProjects/PdfWhitet736/CoreD/"

    @Test
    fun addition_dex() {
        val sourceFilePath = "${pathBASE}makejar/dex/classes.dex" // 源文件路径，可按需修改
        val outputFolderPath = "${pathBASE}output" // 目标文件路径，可按需修改
        val sourceFile = File(sourceFilePath)
        val outputFolder = File(outputFolderPath)
        if (!outputFolder.exists()) {
            outputFolder.mkdirs()
        }

        val file3 = File("$outputFolderPath/readme.md")
        val string = dexToAesText(sourceFile)

        println("文件重写并保存成功")

        // 验证
        file3.writeText(string)
        // aes+iv 加密
        val restoredDex = File(outputFolderPath, "dexMy2.dex")
        val dexBytes = decryptDex(DEX_AES_KEY, file3.readText())
        FileOutputStream(restoredDex).use { it.write(dexBytes) }
    }

    private val DEX_AES_KEY = "Jvklmsd6GNJcdenj".toByteArray() // 16, 24, or 32 bytes


    // DEX -> AES加密文本
    fun dexToAesText(dexFile: File): String {
        val dexBytes = dexFile.readBytes()
        val encrypted = encrypt(dexBytes)
        return Base64.getEncoder().encodeToString(encrypted)
    }


    // 加密（改进版 - 明确指定ECB/PKCS5Padding模式）
    fun encrypt(inputBytes: ByteArray): ByteArray {
        val key = SecretKeySpec(DEX_AES_KEY, ALGORITHM)
        // 明确指定模式，与Android解密保持一致
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val outputBytes = cipher.doFinal(inputBytes)
        println("加密后字节数: ${outputBytes.size}")
        return outputBytes
    }

    // 解密（改进版 - 明确指定ECB/PKCS5Padding模式）
    private fun decryptDex(keyAes: ByteArray, inStr: String): ByteArray {
        println("Base64字符串长度: ${inStr.length}")
        val inputBytes = Base64.getDecoder().decode(inStr)
        println("Base64解码后字节数: ${inputBytes.size}")
        val key = SecretKeySpec(keyAes, ALGORITHM)
        // 明确指定模式，与加密保持一致
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val outputBytes = cipher.doFinal(inputBytes)
        println("解密后字节数: ${outputBytes.size}")
        return outputBytes
    }

}