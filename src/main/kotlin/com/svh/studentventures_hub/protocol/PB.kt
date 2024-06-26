package com.svh.studentventures_hub.protocol

import com.svh.studentventures_hub.dao_party.dao_model.base.DaoStructMethod
import com.svh.studentventures_hub.dao_party.dao_model.base.DaoTools
import com.svh.studentventures_hub.dao_party.dao_model.base.UserStructMethod

class PB {
    companion object {
        //group global
        const val G_STUDENT_CLASS_CODE = "class_code"
        const val G_LOGIN_ACCOUNT = "login_account"
        const val G_LOGIN_PASSWORD = "login_password"

        //group action
        const val A_ACTION = "action"
        const val A_LOGIN_TEST = "login_test"
        const val A_LOGIN = "login"
        const val A_RESET_PSW = "reset_psw"
        const val A_QUERY_VACATION = "query_vacation"
        const val A_RECORD_OPERATION = "record_operation"

        //group replay head
        const val R_STATUS = "status"

        //group session & replay body
        const val S_SESSION = "Authorization"
        const val S_CONTINUE = "continue"
        const val S_OVERDUE = "session_overdue"

        //group login
        const val L_E_C_PASSWORD = "check_password"
        const val L_E_C_ACCOUNT = "check_account"
        const val L_E_INSIDE = "error"

        //group reset password
        const val RS_NEW_PASSWORD = "new_password"
        const val RS_P_E_OP = "error_origin_psw"
        const val RS_P_E_E = "error_entity"
        const val RS_P_E_S = "error_session"


        //group query students
        const val QUERY_TYPE = "query_type"
        const val QUERY_T_VACATION_NOT_R = "query_vacation_not_report"
        const val QUERY_T_VACATION_HISTORY_ALL = "query_vacation_all"
        const val QUERY_T_VACATION_HISTORY_BLENDER = "%timaviciix%"

        //group record operation
        const val RECORD_JSON = "record_json"
        const val RECORD_TYPE = "record_type"
        const val RECORD_OPERATE_SAVE = "save_record"
        const val RECORD_OPERATE_CHANGE = "change_record"
    }
}