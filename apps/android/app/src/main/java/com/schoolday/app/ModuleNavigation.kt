package com.schoolday.app
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
object ModuleNavigation{
 val labels=mapOf("Chats" to mapOf("en" to "Chats","ms" to "Sembang","zh" to "聊天","ta" to "அரட்டை"),
"Calls" to mapOf("en" to "Calls","ms" to "Panggilan","zh" to "通话","ta" to "அழைப்புகள்"),
"Classroom" to mapOf("en" to "Live classes","ms" to "Kelas langsung","zh" to "在线课堂","ta" to "நேரலை வகுப்பு"),
"Files" to mapOf("en" to "Files","ms" to "Fail","zh" to "文件","ta" to "கோப்புகள்"),
"Alerts" to mapOf("en" to "Alerts","ms" to "Makluman","zh" to "提醒","ta" to "அறிவிப்புகள்"),
"Offline" to mapOf("en" to "Offline","ms" to "Luar talian","zh" to "离线","ta" to "இணைப்பில்லா"),
"Quiz" to mapOf("en" to "Quizzes","ms" to "Kuiz","zh" to "测验","ta" to "வினாடி வினா"),
"Gate" to mapOf("en" to "Quiz unlock","ms" to "Buka kunci","zh" to "测验解锁","ta" to "வினா திறப்பு"),
"Courses" to mapOf("en" to "Courses","ms" to "Kursus","zh" to "课程","ta" to "பாடங்கள்"),
"Materials" to mapOf("en" to "Materials","ms" to "Bahan","zh" to "资料","ta" to "பாடப்பொருள்"),
"Assignments" to mapOf("en" to "Tasks","ms" to "Tugasan","zh" to "作业","ta" to "பணிகள்"),
"QuizDraft" to mapOf("en" to "Quiz editor","ms" to "Cipta kuiz","zh" to "测验编辑","ta" to "வினா உருவாக்கு"),
"Assessment" to mapOf("en" to "Assessments","ms" to "Pentaksiran","zh" to "评估","ta" to "மதிப்பீடு"),
"Grades" to mapOf("en" to "Progress","ms" to "Kemajuan","zh" to "学习进度","ta" to "முன்னேற்றம்"),
"Research" to mapOf("en" to "Research","ms" to "Penyelidikan","zh" to "研究","ta" to "ஆராய்ச்சி"),
"Internship" to mapOf("en" to "Training","ms" to "Latihan","zh" to "实习培训","ta" to "பயிற்சி"),
"Calendar" to mapOf("en" to "Calendar","ms" to "Kalendar","zh" to "日历","ta" to "நாள்காட்டி"),
"EventDraft" to mapOf("en" to "Events","ms" to "Acara","zh" to "活动","ta" to "நிகழ்வுகள்"),
"Attendance" to mapOf("en" to "Attendance","ms" to "Kehadiran","zh" to "出勤","ta" to "வருகை"),
"Timetable" to mapOf("en" to "Schedule","ms" to "Jadual","zh" to "课表","ta" to "அட்டவணை"),
"Institutions" to mapOf("en" to "Institutions","ms" to "Institusi","zh" to "机构","ta" to "நிறுவனங்கள்"),
"Activities" to mapOf("en" to "Activities","ms" to "Aktiviti","zh" to "活动","ta" to "செயல்பாடுகள்"),
"Library" to mapOf("en" to "Library","ms" to "Perpustakaan","zh" to "图书馆","ta" to "நூலகம்"),
"Services" to mapOf("en" to "Services","ms" to "Perkhidmatan","zh" to "服务","ta" to "சேவைகள்"),
"Welfare" to mapOf("en" to "Wellbeing","ms" to "Kesejahteraan","zh" to "身心健康","ta" to "நல்வாழ்வு"),
"Administration" to mapOf("en" to "Admin","ms" to "Pentadbiran","zh" to "管理","ta" to "நிர்வாகம்"),
"University" to mapOf("en" to "Academic","ms" to "Akademik","zh" to "教务","ta" to "கல்வி நிர்வாகம்"),
"Rewards" to mapOf("en" to "Rewards","ms" to "Ganjaran","zh" to "奖励","ta" to "வெகுமதிகள்"),
"Certificates" to mapOf("en" to "Certificates","ms" to "Sijil","zh" to "证书","ta" to "சான்றிதழ்கள்"),
"CertificateDraft" to mapOf("en" to "Design","ms" to "Reka sijil","zh" to "证书设计","ta" to "சான்று வடிவமைப்பு"),
"Portfolio" to mapOf("en" to "Portfolio","ms" to "Portfolio","zh" to "档案","ta" to "தொகுப்பு"),
"Credentials" to mapOf("en" to "Credentials","ms" to "Kredensial","zh" to "资历","ta" to "தகுதிகள்"),
"Career" to mapOf("en" to "Career","ms" to "Kerjaya","zh" to "职业","ta" to "தொழில்"),
"Profile" to mapOf("en" to "Profile","ms" to "Profil","zh" to "个人资料","ta" to "சுயவிவரம்"),
"FamilyControls" to mapOf("en" to "Family","ms" to "Keluarga","zh" to "家庭","ta" to "குடும்பம்"),
"Friends" to mapOf("en" to "Connections","ms" to "Hubungan","zh" to "联系人","ta" to "தொடர்புகள்"),
"Security" to mapOf("en" to "Security","ms" to "Keselamatan","zh" to "安全","ta" to "பாதுகாப்பு"),
"Safety" to mapOf("en" to "Safety","ms" to "Perlindungan","zh" to "保护","ta" to "காப்பு"),
"Support" to mapOf("en" to "Help","ms" to "Bantuan","zh" to "帮助","ta" to "உதவி"),
"Connect" to mapOf("en" to "Connect","ms" to "Hubung","zh" to "联系","ta" to "தொடர்பு"),
"Learn" to mapOf("en" to "Learn","ms" to "Belajar","zh" to "学习","ta" to "கற்றல்"),
"Campus" to mapOf("en" to "Community","ms" to "Komuniti","zh" to "社区","ta" to "சமூகம்"),
"Achievements" to mapOf("en" to "Achievements","ms" to "Pencapaian","zh" to "成就","ta" to "சாதனைகள்"),
"Me" to mapOf("en" to "Me","ms" to "Saya","zh" to "我的","ta" to "நான்"))
 val paths=mapOf("Chats" to "M20 11a8 8 0 0 1-8 8H4l2-4a8 8 0 1 1 14-4Z",
"Calls" to "M3 6H15V18H3ZM15 10L21 7V17L15 14",
"Classroom" to "M3 4H21V15H3ZM12 15V20M7 20H17M8 8H16",
"Files" to "M3 6H9L11 8H21V19H3Z",
"Alerts" to "M6 17H18L16 14V9A4 4 0 0 0 8 9V14ZM10 20H14",
"Offline" to "M12 8V20M8 20H16M9 10A4 4 0 0 1 9 4M15 10A4 4 0 0 0 15 4M6 13A8 8 0 0 1 6 2M18 13A8 8 0 0 0 18 2",
"Quiz" to "M6 3H18V21H6ZM9 7H15M9 11H15M9 15H13",
"Gate" to "M6 11H18V21H6ZM9 11V7A3 3 0 0 1 15 7M12 15V18",
"Courses" to "M3 4H10L12 6L14 4H21V19H14L12 21L10 19H3ZM12 6V21",
"Materials" to "M4 4H8V20H4ZM10 4H14V20H10ZM16 5L20 4L23 19L19 20Z",
"Assignments" to "M4 5H20V20H4ZM7 9L9 11L12 7M14 9H17M7 15L9 17L12 13M14 15H17",
"QuizDraft" to "M12 4H4V20H20V12M10 14L19 5L21 7L12 16L9 17Z",
"Assessment" to "M5 3H15L19 7V21H5ZM15 3V7H19M8 11H16M8 15H16",
"Grades" to "M4 20H21M6 20V12H9V20M12 20V8H15V20M18 20V4H21V20",
"Research" to "M10 3A7 7 0 1 0 10 17A7 7 0 1 0 10 3ZM15 15L21 21",
"Internship" to "M3 8H21V20H3ZM8 8V4H16V8M3 13H21M10 12V15H14V12",
"Calendar" to "M3 5H21V21H3ZM7 3V8M17 3V8M3 10H21",
"EventDraft" to "M3 5H21V21H3ZM7 3V8M17 3V8M3 10H21M12 12V18M9 15H15",
"Attendance" to "M12 3A9 9 0 1 0 12 21A9 9 0 1 0 12 3ZM7 12L10 15L17 8",
"Timetable" to "M12 3A9 9 0 1 0 12 21A9 9 0 1 0 12 3ZM12 6V12L16 14",
"Institutions" to "M3 21H21M5 21V7H12V21M12 21V3H19V21M8 10H9M8 14H9M15 7H16M15 11H16",
"Activities" to "M8 3H16V10A4 4 0 0 1 8 10ZM8 6H3V8A5 5 0 0 0 8 13M16 6H21V8A5 5 0 0 1 16 13M12 14V20M7 20H17",
"Library" to "M4 4H20V20H4ZM8 4V20M12 8H17",
"Services" to "M3 3H10V10H3ZM14 3H21V10H14ZM3 14H10V21H3ZM14 14H21V21H14Z",
"Welfare" to "M12 20L4 12A5 5 0 0 1 12 6A5 5 0 0 1 20 12Z",
"Administration" to "M12 3L15 6L19 6L21 10L19 14L19 18L15 21L12 19L9 21L5 18L5 14L3 10L5 6L9 6ZM12 9A3 3 0 1 0 12 15A3 3 0 1 0 12 9Z",
"University" to "M2 8L12 3L22 8L12 13ZM6 10V17Q12 21 18 17V10M22 8V15",
"Rewards" to "M12 3L15 9L22 10L17 15L18 21L12 18L6 21L7 15L2 10L9 9Z",
"Certificates" to "M12 3A6 6 0 1 0 12 15A6 6 0 1 0 12 3ZM8 14L6 21L12 18L18 21L16 14",
"CertificateDraft" to "M4 4H16V20H4ZM9 14L19 4L21 6L11 16L8 17Z",
"Portfolio" to "M3 6H9L11 8H21V19H3ZM8 12H16M8 15H14",
"Credentials" to "M12 2L15 5L19 5L19 9L22 12L19 15L19 19L15 19L12 22L9 19L5 19L5 15L2 12L5 9L5 5L9 5ZM8 12L11 15L16 9",
"Career" to "M3 8H21V20H3ZM8 8V4H16V8M3 13H21",
"Profile" to "M12 3A9 9 0 1 0 12 21A9 9 0 1 0 12 3ZM12 7A3 3 0 1 0 12 13A3 3 0 1 0 12 7ZM6 18Q12 12 18 18",
"FamilyControls" to "M8 4A3 3 0 1 0 8 10A3 3 0 1 0 8 4ZM16 6A2 2 0 1 0 16 10A2 2 0 1 0 16 6ZM2 20V16Q8 10 14 16V20M14 13Q20 11 22 16V20",
"Friends" to "M8 4A3 3 0 1 0 8 10A3 3 0 1 0 8 4ZM16 4A3 3 0 1 0 16 10A3 3 0 1 0 16 4ZM2 20V16Q8 11 14 16V20M14 13Q20 11 22 16V20",
"Security" to "M12 3L20 6V12Q20 18 12 22Q4 18 4 12V6Z",
"Safety" to "M12 3L20 6V12Q20 18 12 22Q4 18 4 12V6ZM12 8V13M12 16V17",
"Support" to "M12 3A9 9 0 1 0 12 21A9 9 0 1 0 12 3ZM9 9A3 3 0 0 1 15 9Q15 11 12 12V14M12 17V18")
 fun label(id:String,lang:String)=labels[id]?.get(lang)?:labels[id]?.get("en")?:id
}
@Composable fun ModuleIcon(id:String,color:Color,modifier:Modifier=Modifier){
 val path=remember(id){PathParser().parsePathString(ModuleNavigation.paths[id]?:"M4 4H20V20H4Z").toPath()}
 Canvas(modifier.size(23.dp)){scale(size.width/24f,size.height/24f,pivot=Offset.Zero){drawPath(path,color,style=Stroke(width=1.6f,cap=StrokeCap.Round,join=StrokeJoin.Round))}}
}
