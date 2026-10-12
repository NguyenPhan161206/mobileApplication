class Menu(
    val functions: List<Class<*>> = listOf(BasicCalculate_b1::class.java, BasicCalculate_b2::class.java),
    var routing: Routing = Routing(BasicCalculate_b1::class.java)
) { // khai báo một class Menu với một thuộc tính functions là một danh sách các class, mặc định là BasicCalculate_b1 và BasicCalculate_b2

    fun conflictSolving(chosen: Int, functions: Int): Int = if (chosen <= functions && chosen > 0) 1 else 0
    fun nextStep(chosen: Int): String = if (chosen == 1) "You have chosen BasicCalculate_b1" else if (chosen == 2) "You have chosen BasicCalculate_b2" else "Invalid choice"
    // Viết hàm đếm số lượng các hàm trong một class
    fun countFunctions(cls: Class<*>): Int {
        return cls.declaredMethods.size
    }
    fun perform(){
        println("Please choose a function:")
        for ((index, cls) in functions.withIndex()) {
            println("${index + 1}. ${cls.simpleName}")
        }
    }
    fun classChoose(): Int{
        perform()
        val chosen = readLine()?.toIntOrNull() ?: 0
        if (chosen <= 0 || chosen > functions.size) {
            println("Invalid choice. Please try again.")
            return classChoose()
        }
        return chosen
    }
    fun chooseInClass(cls: Class<*>): String {
        val methods = cls.declaredMethods
            .filter { !it.isSynthetic }
            .sortedBy { it.name }
        println("Please choose a function:")
        for ((index, method) in methods.withIndex()) {
            println("${index + 1}. ${method.name}")
        }
        val chosen = readLine()?.toIntOrNull() ?: 0
        if (chosen <= 0 || chosen > methods.size) {
            println("Invalid choice. Please try again.")
            return chooseInClass(cls)
        }
        return methods[chosen - 1].name
    }
    companion object manager {
        fun run() {
            val menu = Menu()
            val num = menu.classChoose()
            val cls = menu.functions[num - 1]
            val routing = Routing(cls)
            val methodName = menu.chooseInClass(cls)
            routing.routingFunction(methodName)
        }
    }
}
    // viết hàm để chọn class cần chạy trong folder exercises, sau đó gọi hàm countFunctions để đếm số lượng các hàm trong class đó


fun main(){
    // Khởi tạo một instance của Menu
    Menu.manager.run()
}
