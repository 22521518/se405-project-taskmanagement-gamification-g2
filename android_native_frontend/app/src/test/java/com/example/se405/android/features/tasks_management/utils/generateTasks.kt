@file:OptIn(ExperimentalUuidApi::class)

package com.example.se405.android.features.tasks_management.utils

import com.example.se405.android.features.tasks_management.domain.entity.Task
import com.example.se405.android.features.tasks_management.domain.entity.TaskPriority
import com.example.se405.android.features.tasks_management.domain.entity.TaskStatus
import com.example.se405.android.features.tasks_management.domain.entity.TaskType
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val sampleTitles = listOf(
    "Chạy bộ buổi sáng", "Đọc sách phát triển bản thân", "Uống nước lọc", "Thiền định", "Học từ vựng tiếng Anh",
    "Lập kế hoạch ngày mới", "Review code bản thân", "Viết nhật ký", "Dọn dẹp bàn làm việc", "Tập giãn cơ",
    "Học thêm kiến thức Kotlin", "Nghe podcast công nghệ", "Kiểm tra email công việc", "Đi ngủ đúng giờ", "Hạn chế mạng xã hội",
    "Chuẩn bị bữa ăn lành mạnh", "Luyện gõ bàn phím nhanh", "Cập nhật danh sách chi tiêu", "Gọi điện cho gia đình", "Tưới cây ban công"
)

val sampleDescriptions = listOf(
    "Duy trì đều đặn để cải thiện sức khỏe thể chất.", "Dành ra ít nhất 15-30 phút không điện thoại.", "Bổ sung đủ lượng nước cơ thể cần trong ngày.", "Giúp tâm trí tĩnh lặng và tập trung tốt hơn.", "Học và đặt câu với ít nhất 5 từ mới.",
    "Liệt kê 3 việc quan trọng nhất cần hoàn thành.", "Xem lại các dòng code cũ để tối ưu hóa.", "Ghi lại những điều tích cực và bài học hôm nay.", "Không gian sạch sẽ giúp làm việc hiệu quả hơn.", "Giảm căng thẳng cho các cơ sau giờ ngồi máy tính.",
    "Đọc documentation hoặc xem tutorial mới.", "Cập nhật xu hướng công nghệ mới nhất khi di chuyển.", "Xử lý các email quan trọng tránh tồn đọng.", "Tắt thiết bị điện tử trước 30 phút.", "Giới hạn thời gian lướt feed dưới 15 phút.",
    "Tự nấu ăn với nhiều rau xanh và protein.", "Luyện tập trên các trang web như Monkeytype.", "Ghi lại mọi khoản thu chi phát sinh trong ngày.", "Kết nối và chia sẻ với những người thân yêu.", "Chăm sóc mảng xanh nhỏ trong không gian sống."
)

fun getTimeSuffix(): String = if (Random.nextBoolean()) " (${Random.nextInt(1, 31)} ngày)" else " (${Random.nextInt(1, 24)} giờ)"

fun generateHabitTasks(count: Int): List<Task> {
    val tasks = mutableListOf<Task>()
    val priorities =
        TaskPriority.entries.toTypedArray() // Lấy tất cả các trạng thái Priority có sẵn

    for (i in 0 until count) {
        val baseTitle = sampleTitles[i % sampleTitles.size]
        val description = sampleDescriptions[i % sampleDescriptions.size]

        val timeSuffix = getTimeSuffix()
        val finalTitle = baseTitle + timeSuffix
        val randomPriority = priorities[Random.nextInt(priorities.size)]

        val task = Task(
            uuid = Uuid.random(),
            title = finalTitle,
            description = description,
            repetition = Random.nextInt(1, 7),
            type = TaskType.HABIT,
            status = TaskStatus.TODO,
            priority = randomPriority,
            creator = null,
            tags = emptyList(),
            taskCompletionLog = emptyList(),
            startDate = LocalDate.now()
        )
        tasks.add(task)
    }
    return tasks
}

class GenerateTasksTest {
    @Test
    fun testGenerateHabitTasks() {
        generateHabitTasks(10).forEach { tsk ->
            println(tsk.toString())
        }
    }
}