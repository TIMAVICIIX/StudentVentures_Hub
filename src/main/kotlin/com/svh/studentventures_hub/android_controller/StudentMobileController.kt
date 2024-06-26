package com.svh.studentventures_hub.android_controller

import com.google.gson.Gson
import com.svh.studentventures_hub.dao_party.dao_model.StudentDAO
import com.svh.studentventures_hub.dao_party.object_model.info.Student
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import com.google.gson.JsonObject
import com.svh.studentventures_hub.dao_party.dao_model.VentureDAO
import com.svh.studentventures_hub.dao_party.dao_model.VentureRecordDAO
import com.svh.studentventures_hub.dao_party.dao_model.base.DaoTools.Companion.toStringOrBlank
import com.svh.studentventures_hub.dao_party.object_model.venture.Venture
import com.svh.studentventures_hub.dao_party.object_model.venture.VentureRecord
import com.svh.studentventures_hub.listener.SessionManager
import com.svh.studentventures_hub.protocol.PB
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@WebServlet(name = "StudentMobileController", value = ["/mobile_student-controller"])
class StudentMobileController : HttpServlet() {

    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?) {
        resp?.contentType = "application/json;charset=UTF-8"
        req?.characterEncoding = "utf-8"

        //DEBUG
        println("Fetch Request!")

        if (resp != null && req != null) {
            when (req.getParameter(PB.A_ACTION)) {
                PB.A_LOGIN_TEST -> loginTest(req, resp)
                PB.A_LOGIN -> login(req, resp)
                PB.A_RESET_PSW -> resetPassword(req, resp)
                PB.A_QUERY_VACATION -> queryVacation(req, resp)
                PB.A_RECORD_OPERATION -> recordSaveAndUpdate(req, resp)
            }
        }
    }

    private fun loginTest(req: HttpServletRequest, resp: HttpServletResponse) {

        val account = req.getParameter(PB.G_LOGIN_ACCOUNT)
        val password = req.getParameter(PB.G_LOGIN_PASSWORD)

        val checkStudent: Student? = StudentDAO().exactQuery(account)

        val jsonStatus = JsonObject()

        if (checkStudent != null) {
            if (checkStudent.password == password) {
                jsonStatus.addProperty(PB.R_STATUS, PB.S_CONTINUE)
            } else {
                jsonStatus.addProperty(PB.R_STATUS, PB.L_E_C_PASSWORD)
            }
        } else {
            jsonStatus.addProperty(PB.R_STATUS, PB.L_E_C_ACCOUNT)
        }

        resp.writer?.apply {
            kotlin.io.println(jsonStatus.toStringOrBlank())
            write(jsonStatus.toStringOrBlank())
            flush()
            return
        }

    }

    private fun login(req: HttpServletRequest, resp: HttpServletResponse) {

        val account = req.getParameter(PB.G_LOGIN_ACCOUNT)
        val password = req.getParameter(PB.G_LOGIN_PASSWORD)

        val studentUser = StudentDAO().exactQuery(account)

        val jsonStudentPhaser = Gson()

        if (studentUser != null && studentUser.password == password) {
            SessionManager.destroy(studentUser.studentCode)

            req.session.apply {

                maxInactiveInterval = 30000000
                println("Register Session:${this.id}")
                SessionManager.associateUserWithSession(studentUser.studentCode, this)

            }

            resp.writer?.apply {
                kotlin.io.println(jsonStudentPhaser.toJson(studentUser))
                write(jsonStudentPhaser.toJson(studentUser))
                flush()
                return
            }

        } else {
            resp.writer?.apply {
                write(JsonObject().addProperty(PB.R_STATUS, PB.L_E_INSIDE).toStringOrBlank())
                flush()
                return
            }
        }

    }

    private fun resetPassword(req: HttpServletRequest, resp: HttpServletResponse) {

        val authSessionID = req.getParameter(PB.S_SESSION)
        println("Quest Session:${req.session.id}")
        println("Param Session:${req.session.id}")

        val jsonStatus = JsonObject()

        if (SessionManager.checkSession(authSessionID)) {

            val studentID = req.getParameter(PB.G_LOGIN_ACCOUNT)
            val originPsw = req.getParameter(PB.G_LOGIN_PASSWORD)
            val newPsw = req.getParameter(PB.RS_NEW_PASSWORD)

            val studentDAO = StudentDAO()
            val studentObject = studentDAO.exactQuery(studentID)

            if (studentObject != null) {
                if (studentObject.password == originPsw) {
                    studentDAO.reSetPassword(studentID, newPsw)
                    jsonStatus.addProperty(PB.R_STATUS, PB.S_CONTINUE)
                } else {
                    jsonStatus.addProperty(PB.R_STATUS, PB.RS_P_E_OP)
                }
            } else {
                jsonStatus.addProperty(PB.R_STATUS, PB.RS_P_E_E)
            }

        } else {
            jsonStatus.addProperty(PB.R_STATUS, PB.RS_P_E_S)
        }

        resp.writer?.apply {
            //kotlin.io.println(jsonStatus.toStringOrBlank())
            write(jsonStatus.toStringOrBlank())
            flush()
            return
        }

    }

    private fun queryVacation(req: HttpServletRequest, resp: HttpServletResponse) {

        println("查询假期服务启动!")

        val authSessionID = req.getParameter(PB.S_SESSION)

        val gson = Gson()
        var jsonStatus = ""

        if (SessionManager.checkSession(authSessionID)) {

            println("Session验证成功!开始查询")

            val studentCode = req.getParameter(PB.G_LOGIN_ACCOUNT)
            val classCode = req.getParameter(PB.G_STUDENT_CLASS_CODE)

            val ventureMutableList: MutableList<Venture> = mutableListOf()

            val ventureDAO = VentureDAO()
            val ventureRecordDAO = VentureRecordDAO()
            val allVentureList = ventureDAO.stuFilterGetVentures(classCode, "LIMIT_DATE")
            val historyVentureList = ventureDAO.stuFilterGetVentures(classCode, "LIMITLESS_DATE")

            val stuVentureRecordMutableList: MutableList<VentureRecord> = mutableListOf()

            historyVentureList.forEach {
                stuVentureRecordMutableList += ventureRecordDAO.authFilterQuery(it.ventureCode, studentCode)
            }

            val stuRecordString = ventureRecordDAO.getStuRecordString(studentCode)

            val exampleRecord = VentureRecord()

            val exampleVenture = Venture(
                " ",
                " ",
                " ",
                " ",
                " ",
                " ",
                " ",
                " ",
                " ",
                " "
            )



            when (req.getParameter(PB.QUERY_TYPE)) {

                PB.QUERY_T_VACATION_NOT_R -> {


                    for (venture in allVentureList) {
                        if (venture.ventureCode !in stuRecordString) {
                            venture.ventureState = "填写中"
                            ventureMutableList.add(venture)
                        }
                    }
                    ventureMutableList.add(exampleVenture)
                    jsonStatus = gson.toJson(ventureMutableList).toStringOrBlank()
                }

                PB.QUERY_T_VACATION_HISTORY_ALL -> {
                    val currentDateString = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val currentDate = LocalDate.parse(currentDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))


                    for (venture in historyVentureList) {

                        if (venture.ventureCode in stuRecordString) {
                            val givenDate =
                                LocalDate.parse(venture.ventureEndDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            if (currentDate.isAfter(givenDate)) {
                                venture.ventureState = "已过期"
                            } else {
                                println("${studentCode}下查询到一条可修改记录:${venture.ventureCode}")
                                venture.ventureState = "可修改"
                            }
                            ventureMutableList.add(venture)
                        } else {
                            if (venture.ventureState == "已过期") {
                                ventureMutableList.add(venture)
                            }
                        }

                    }
                    ventureMutableList.add(exampleVenture)
                    stuVentureRecordMutableList.add(exampleRecord)
                    jsonStatus = gson.toJson(ventureMutableList).toStringOrBlank() +
                            PB.QUERY_T_VACATION_HISTORY_BLENDER +
                            gson.toJson(stuVentureRecordMutableList).toStringOrBlank()
                }
            }

        } else {
            jsonStatus = JsonObject().addProperty(PB.S_SESSION, PB.S_OVERDUE).toStringOrBlank()
        }

        resp.writer?.apply {
            kotlin.io.println(jsonStatus.toStringOrBlank())
            write(jsonStatus)
            flush()
            return
        }

    }

    private fun recordSaveAndUpdate(req: HttpServletRequest, resp: HttpServletResponse) {

        val authSessionID = req.getParameter(PB.S_SESSION)
        val jsonStatus = JsonObject()


        if (SessionManager.checkSession(authSessionID)) {
            val recordEntity = Gson().fromJson(req.getParameter(PB.RECORD_JSON), VentureRecord::class.java)
            val ventureRecordDAO = VentureRecordDAO()
            val operationType = req.getParameter(PB.RECORD_TYPE)

            when (operationType) {
                PB.RECORD_OPERATE_SAVE -> {
                    ventureRecordDAO.save(recordEntity)
                    jsonStatus.addProperty(PB.R_STATUS, PB.S_CONTINUE)
                }

                PB.RECORD_OPERATE_CHANGE -> {
                    ventureRecordDAO.update(recordEntity)
                    jsonStatus.addProperty(PB.R_STATUS, PB.S_CONTINUE)
                }
            }

        } else {
            jsonStatus.addProperty(PB.R_STATUS, PB.S_OVERDUE)
        }

        resp.writer?.apply {
            write(jsonStatus.toStringOrBlank())
            flush()
            return
        }

    }

}