import java.io.File

class Routing(
    val targetClass: Class<*>,
    private val configPath: String = "exercises/routing.json"
) {

    // Khởi tạo instance 1 lần duy nhất để tái sử dụng
    private val instance: Any by lazy {
        targetClass.getDeclaredConstructor().newInstance()
    }

    // Nạp cấu hình JSON: { "TenClass": { "tenHam": ["tenThamSo1", "tenThamSo2"] } }
    private val config: Map<String, Any?> by lazy {
        val file = File(configPath)
        if (!file.exists()) {
            println("Không tìm thấy file cấu hình: ${file.absolutePath}")
            emptyMap()
        } else {
            @Suppress("UNCHECKED_CAST")
            (Json.parse(file.readText()) as? Map<String, Any?>) ?: emptyMap()
        }
    }

    // Index động: tên tham số lấy từ JSON (s, s1, s2...), giá trị do người dùng nhập
    fun routingFunction(methodName: String) {
        @Suppress("UNCHECKED_CAST")
        val classConfig = config[targetClass.simpleName] as? Map<String, Any?>
        if (classConfig == null) {
            println("Không có cấu hình cho class ${targetClass.simpleName}")
            return
        }

        val candidates = targetClass.methods.filter { it.name == methodName && !it.isSynthetic }
        if (candidates.isEmpty()) {
            println("Lỗi: Class ${targetClass.simpleName} không có hàm $methodName")
            return
        }

        @Suppress("UNCHECKED_CAST")
        val declaredNames = classConfig[methodName] as? List<Any?>
        val method = declaredNames
            ?.let { names -> candidates.firstOrNull { it.parameterCount == names.size } }
            ?: candidates.first()
        val paramNames = declaredNames
            ?.map { it?.toString() ?: "arg" }
            ?: method.parameterTypes.map { it.simpleName }

        val args = method.parameterTypes.mapIndexed { index, type ->
            readArgument(paramNames.getOrElse(index) { "arg${index + 1}" }, type)
        }.toTypedArray()

        try {
            val result = method.invoke(instance, *args)
            println("Kết quả: $result")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Hỏi người dùng nhập giá trị cho từng tham số theo tên (s, s1, s2...)
    private fun readArgument(name: String, type: Class<*>): Any {
        while (true) {
            print("Nhập $name (${type.simpleName}): ")
            val raw = readLine() ?: ""
            val parsed = parseByType(raw, type)
            if (parsed != null) return parsed
            println("Giá trị không hợp lệ cho kiểu ${type.simpleName}, vui lòng thử lại.")
        }
    }

    private fun parseByType(raw: String, type: Class<*>): Any? = when (type) {
        Int::class.javaPrimitiveType, Int::class.javaObjectType -> raw.trim().toIntOrNull()
        Long::class.javaPrimitiveType, Long::class.javaObjectType -> raw.trim().toLongOrNull()
        Double::class.javaPrimitiveType, Double::class.javaObjectType -> raw.trim().toDoubleOrNull()
        Float::class.javaPrimitiveType, Float::class.javaObjectType -> raw.trim().toFloatOrNull()
        Boolean::class.javaPrimitiveType, Boolean::class.javaObjectType -> raw.trim().toBooleanStrictOrNull()
        else -> raw
    }

    // Nếu chỉ cần trả về Class hiện tại: dùng trực tiếp property targetClass
}

// Bộ parse JSON tối giản, không cần thư viện ngoài
object Json {
    fun parse(text: String): Any? {
        val parser = Parser(text)
        parser.skipWhitespace()
        return parser.parseValue()
    }
    private class Parser(private val s: String) {
        private var i = 0

        fun skipWhitespace() {
            while (i < s.length && s[i].isWhitespace()) i++
        }

        fun parseValue(): Any? {
            skipWhitespace()
            if (i >= s.length) error("JSON rỗng hoặc kết thúc bất ngờ")
            return when (val c = s[i]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> parseString()
                't', 'f' -> parseBoolean()
                'n' -> parseNull()
                else -> if (c == '-' || c.isDigit()) parseNumber() else error("Ký tự không hợp lệ '$c' tại vị trí $i")
            }
        }

        private fun parseObject(): Map<String, Any?> {
            val map = LinkedHashMap<String, Any?>()
            i++ // '{'
            skipWhitespace()
            if (i < s.length && s[i] == '}') { i++; return map }
            while (true) {
                skipWhitespace()
                val key = parseString()
                skipWhitespace()
                expect(':')
                map[key] = parseValue()
                skipWhitespace()
                when (s[i]) {
                    ',' -> { i++; continue }
                    '}' -> { i++; break }
                    else -> error("Mong đợi ',' hoặc '}' tại vị trí $i")
                }
            }
            return map
        }

        private fun parseArray(): List<Any?> {
            val list = ArrayList<Any?>()
            i++ // '['
            skipWhitespace()
            if (i < s.length && s[i] == ']') { i++; return list }
            while (true) {
                list.add(parseValue())
                skipWhitespace()
                when (s[i]) {
                    ',' -> { i++; continue }
                    ']' -> { i++; break }
                    else -> error("Mong đợi ',' hoặc ']' tại vị trí $i")
                }
            }
            return list
        }

        private fun parseString(): String {
            expect('"')
            val sb = StringBuilder()
            while (true) {
                val c = s[i++]
                when (c) {
                    '"' -> break
                    '\\' -> when (val e = s[i++]) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'n' -> sb.append('\n')
                        't' -> sb.append('\t')
                        'r' -> sb.append('\r')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'u' -> { val hex = s.substring(i, i + 4); i += 4; sb.append(hex.toInt(16).toChar()) }
                        else -> sb.append(e)
                    }
                    else -> sb.append(c)
                }
            }
            return sb.toString()
        }

        private fun parseNumber(): Any {
            val start = i
            if (s[i] == '-') i++
            while (i < s.length && (s[i].isDigit() || s[i] == '.' || s[i] == 'e' || s[i] == 'E' || s[i] == '+' || s[i] == '-')) i++
            val num = s.substring(start, i)
            return if (num.contains('.') || num.contains('e') || num.contains('E')) num.toDouble() else num.toLong()
        }

        private fun parseBoolean(): Boolean = when {
            s.startsWith("true", i) -> { i += 4; true }
            s.startsWith("false", i) -> { i += 5; false }
            else -> error("Giá trị boolean không hợp lệ tại vị trí $i")
        }

        private fun parseNull(): Any? {
            if (!s.startsWith("null", i)) error("Giá trị null không hợp lệ tại vị trí $i")
            i += 4
            return null
        }

        private fun expect(c: Char) {
            if (i >= s.length || s[i] != c) error("Mong đợi '$c' tại vị trí $i")
            i++
        }
    }
}
