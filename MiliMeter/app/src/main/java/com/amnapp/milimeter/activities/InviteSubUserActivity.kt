package com.amnapp.milimeter.activities

import android.content.DialogInterface
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.amnapp.milimeter.AccountManager
import com.amnapp.milimeter.GroupMemberData
import com.amnapp.milimeter.R
import com.amnapp.milimeter.databinding.ActivityInviteSubUserBinding

class InviteSubUserActivity : CustomThemeActivity() {
    lateinit var binding: ActivityInviteSubUserBinding
    lateinit var mLoadingDialog: AlertDialog//로딩화면임. setProgressDialog()를 실행후 mDialog.show()로 시작
    lateinit var mParentGroupMemberData: GroupMemberData
    var mChildGroupMemberData: GroupMemberData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadTheme()

        binding = ActivityInviteSubUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        setProgressDialog()

        mParentGroupMemberData = intent.extras!!.getSerializable(GroupMemberData.GROUP_MEMBER_PARENT) as GroupMemberData
        mChildGroupMemberData = intent.extras!!.getSerializable(GroupMemberData.GROUP_MEMBER_CHILD) as GroupMemberData?

        val fillEmpty = intent.extras!!.getBoolean(FILL_EMPTY_ACCOUNT, false)

        binding.adminCb.isChecked = mChildGroupMemberData?.admin ?: false

        binding.inviteBt.setOnClickListener {
            binding.inviteBt.isClickable = false
            mLoadingDialog.show()

            if(fillEmpty){
                AccountManager().fillEmptyAccount(
                    applicationContext,
                    mChildGroupMemberData!!,
                    binding.subUserIdEt.text.toString(),
                    binding.adminCb.isChecked
                ){resultMessage ->
                    when(resultMessage){
                        AccountManager.ERROR_NETWORK_NOT_CONNECTED->{
                            showDialogMessage("오류", "네트워크 연결을 학인해 주세요"){}
                        }
                        AccountManager.ERROR_NOT_FOUND_ID->{
                            showDialogMessage("오류", "해당 아이디가 존재하지 않습니다"){}
                        }
                        AccountManager.ERROR_GROUPED_ID->{
                            showDialogMessage("오류", "해당 아이디가 이미 그룹에 가입되어 있습니다"){}
                        }
                        AccountManager.RESULT_SUCCESS->{
                            showDialogMessage("성공", "하위유저가 그룹에 가입되었습니다"){}
                        }
                    }
                    mLoadingDialog.dismiss()
                    binding.inviteBt.isClickable = true
                }
            }
            else{
                AccountManager().inviteSubUser(
                    applicationContext,
                    mParentGroupMemberData,
                    binding.subUserIdEt.text.toString(),
                    binding.adminCb.isChecked
                ){resultMessage ->
                    when(resultMessage){
                        AccountManager.ERROR_NETWORK_NOT_CONNECTED->{
                            showDialogMessage("오류", "네트워크 연결을 학인해 주세요"){}
                        }
                        AccountManager.ERROR_NOT_FOUND_ID->{
                            showDialogMessage("오류", "해당 아이디가 존재하지 않습니다"){}
                        }
                        AccountManager.ERROR_GROUPED_ID->{
                            showDialogMessage("오류", "해당 아이디가 이미 그룹에 가입되어 있습니다"){}
                        }
                        AccountManager.RESULT_SUCCESS->{
                            showDialogMessage("성공", "하위유저가 그룹에 가입되었습니다"){}
                        }
                    }
                    mLoadingDialog.dismiss()
                    binding.inviteBt.isClickable = true
                }
            }
        }

        binding.cancelIb.setOnClickListener{
            finish()
        }
        binding.backIb.setOnClickListener{
            finish()
        }
    }

    fun setProgressDialog() {
        val llPadding = 30
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam
        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(this)
        tvText.text = "Loading ..."
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20f
        tvText.layoutParams = llParam
        ll.addView(progressBar)
        ll.addView(tvText)
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(ll)
        mLoadingDialog = builder.create()
    }

    fun showDialogMessage(title: String, body: String, callBack: ()-> Unit) {//다이얼로그 메시지를 띄우는 함수
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(body)
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int ->
            callBack()
        }
        builder.show()
    }

    companion object{
        const val FILL_EMPTY_ACCOUNT = "fill empty account"
    }
}