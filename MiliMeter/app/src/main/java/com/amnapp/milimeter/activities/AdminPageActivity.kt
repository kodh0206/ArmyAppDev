package com.amnapp.milimeter.activities

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.amnapp.milimeter.*
import com.amnapp.milimeter.databinding.ActivityAdminPageBinding
import com.amnapp.milimeter.recyclerViews.AdminPageRecyclerAdapter
import com.amnapp.milimeter.viewModels.AdminPageViewModel
import com.github.mikephil.charting.charts.LineChart

class AdminPageActivity : CustomThemeActivity() {

    lateinit var binding: ActivityAdminPageBinding
    lateinit var mLoadingDialog: AlertDialog//로딩화면임. setProgressDialog()를 실행후 mLoadingDialog.show()로 시작
    private val viewModel: AdminPageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadTheme()
        binding = ActivityAdminPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        setProgressDialog()// 로딩다이얼로그 세팅
        mLoadingDialog.show()//로딩 시작
        renewUI()// Master 권한 여부에 따라 화면 변환

        binding.pathEt.setSelection(binding.pathEt.length())//항상 마지막 경로로 커서가 오도록 설정
        binding.subUserListRv.layoutManager = LinearLayoutManager(this)// 리사이클러뷰 리스트에 레이아웃 매니저를 설정

        viewModel.parentName.observe(this, Observer { // 현재부모가 디렉토리 이동으로 바뀌면 그 이름으로 UI갱신
            binding.parentNameTv.text = it
        })
        viewModel.userPathList.observe(this, Observer {
            var path = ""
            for (i in it){
                path += "/"+if(i.name.isNullOrEmpty()) "(빈 자리)" else i.name
            }
            binding.pathEt.setText(path)
        })
        viewModel.subUserList.observe(this, Observer {
            if(it == null) return@Observer
            val adminPageRecyclerAdapter = AdminPageRecyclerAdapter(it)
            //아이템 내용 클릭 리스너 등록
            adminPageRecyclerAdapter.setItemOnClickListener(object: AdminPageRecyclerAdapter.OnItemClickListener{
                override fun onClicked(v: View, pos: Int) {
                    mLoadingDialog.show() // 로딩 시작
                    viewModel.downDirectory(it[pos], viewModel.subGroupMemberList.value!![pos])
                }
            })
            //아이템 편집 클릭 리스너 등록
            adminPageRecyclerAdapter.setEditOnClickListener(object: AdminPageRecyclerAdapter.OnEditClickListener{
                override fun onClicked(v: View, pos: Int) {
                    val childUserData = it[pos]
                    val parentGroupMemberData = viewModel.groupMemberPathList.value!!.last()
                    val childGroupMemberData = viewModel.subGroupMemberList.value!![pos]
                    if(childGroupMemberData!!.id.isNullOrEmpty()){
                        val intent = Intent(this@AdminPageActivity, EditEmptyInfoActivity::class.java)
                        intent.putExtra(GroupMemberData.GROUP_MEMBER_PARENT, parentGroupMemberData)
                        intent.putExtra(GroupMemberData.GROUP_MEMBER_CHILD, childGroupMemberData)
                        startActivity(intent)
                    }
                    else{
                        val intent = Intent(this@AdminPageActivity, EditSubUserInfoActivity::class.java)
                        intent.putExtra(UserData.USER_CHILD, childUserData)
                        intent.putExtra(GroupMemberData.GROUP_MEMBER_PARENT, parentGroupMemberData)
                        intent.putExtra(GroupMemberData.GROUP_MEMBER_CHILD, childGroupMemberData)
                        startActivity(intent)
                    }

                }

            })
            //차트 클릭 리스너 등록
            adminPageRecyclerAdapter.setChartOnClickListener(object: AdminPageRecyclerAdapter.OnChartClickListener{
                override fun onClicked(v: View, pos: Int) {
                    val childUserData = it[pos]
                    showChartDialog(childUserData)
                }
            })
            binding.subUserListRv.adapter = adminPageRecyclerAdapter//로드된 데이터를 담은 어댑터를 뷰에 탑재
            mLoadingDialog.dismiss() //로딩 해제
        })

        binding.upDirectoryIv.setOnClickListener {
            if (viewModel.userPathList.value?.size == 1) return@setOnClickListener // 최상위 경로면 작동 안한다
            mLoadingDialog.show()//로딩 시작
            viewModel.upDirectory()
        }
        binding.chageGroupCodeBt.setOnClickListener {
            showEditTextDialogMessage("새로운 그룹코드를 입력해주세요"){input ->
                if(input.isNullOrEmpty()){
                    showDialogMessage("오류", "값이 비어있습니다")
                    return@showEditTextDialogMessage
                }
                binding.chageGroupCodeBt.isClickable = false
                mLoadingDialog.show()
                AccountManager().resetGroupCode(applicationContext, input){resultMessage ->
                    when(resultMessage){
                        AccountManager.ERROR_NETWORK_NOT_CONNECTED ->{
                            showDialogMessage("오류", "네트워크 연결을 확인해주세요")
                        }
                        AccountManager.RESULT_SUCCESS ->{
                            showDialogMessage("성공", "그룹코드를 변경했습니다")
                            onResume()
                        }
                    }
                    mLoadingDialog.dismiss()
                    binding.chageGroupCodeBt.isClickable = true
                }
            }
        }
        binding.cancelIb.setOnClickListener {
            finish()
        }
        binding.backIb.setOnClickListener {
            if (viewModel.userPathList.value?.size == 1) return@setOnClickListener // 최상위 경로면 작동 안한다
            mLoadingDialog.show()//로딩 시작
            viewModel.upDirectory()
        }
    }

    private fun renewUI(){
        binding.chageGroupCodeBt.visibility = if(AccountManager.mMaster) View.VISIBLE else View.GONE
    }

    private fun showChartDialog(userData: UserData) {//기록 다이얼로그 띄움
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_sub_user_chart)
        val param = dialog.window!!.attributes//다이얼로그의 파라미터
        param.width = ViewGroup.LayoutParams.MATCH_PARENT
        param.height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog.window!!.attributes = param//변경한 파라미터 적용

        val subUserLc = dialog.findViewById<LineChart>(R.id.subUserLc)
        val cancelIb = dialog.findViewById<ImageButton>(R.id.cancelIb)
        val prevDateLl = dialog.findViewById<LinearLayout>(R.id.prevDateLl)
        val nextDateLl = dialog.findViewById<LinearLayout>(R.id.nextDateLl)
        val dateRangeSp = dialog.findViewById<Spinner>(R.id.dateRangeSp)
        val moveRangeSp = dialog.findViewById<Spinner>(R.id.moveRangeSp)
        val dateRangeTv = dialog.findViewById<TextView>(R.id.dateRangeTv)

        val cm = ChartManager()
        viewModel.chartDate = cm.getCurrentDateBasedOnFormat()//차트에서의 기준일
        cm.loadTrainingRecordNDaysAgo(userData,viewModel.chartDate!!,viewModel.dateRange)
        { docs, lineDataSets, dateList ->
            cm.makeLineChart(subUserLc, lineDataSets, dateList)
            val dateFrom = cm.calculateDate(viewModel.chartDate!!, 1-viewModel.dateRange)//차트 시작일
            dateRangeTv.text = dateFrom+"~"+viewModel.chartDate
        }

        cancelIb.setOnClickListener {
            dialog.dismiss()
        }
        prevDateLl.setOnClickListener {
            viewModel.chartDate = cm.calculateDate(viewModel.chartDate!!, -viewModel.moveRange)
            cm.loadTrainingRecordNDaysAgo(userData,viewModel.chartDate!!,viewModel.dateRange)
            { docs, lineDataSets, dateList ->
                cm.makeLineChart(subUserLc, lineDataSets, dateList)
                val dateFrom = cm.calculateDate(viewModel.chartDate!!, 1-viewModel.dateRange)//차트 시작일
                dateRangeTv.text = dateFrom+"~"+viewModel.chartDate
            }
        }
        nextDateLl.setOnClickListener {
            viewModel.chartDate = cm.calculateDate(viewModel.chartDate!!, viewModel.moveRange)
            cm.loadTrainingRecordNDaysAgo(userData,viewModel.chartDate!!,viewModel.dateRange)
            { docs, lineDataSets, dateList ->
                cm.makeLineChart(subUserLc, lineDataSets, dateList)
                val dateFrom = cm.calculateDate(viewModel.chartDate!!, 1-viewModel.dateRange)//차트 시작일
                dateRangeTv.text = dateFrom+"~"+viewModel.chartDate
            }
        }
        dateRangeSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0-> viewModel.dateRange = 30
                    1-> viewModel.dateRange = 10
                    2-> viewModel.dateRange = 5
                    3-> viewModel.dateRange = 1
                }
                cm.loadTrainingRecordNDaysAgo(userData,viewModel.chartDate!!,viewModel.dateRange)
                { docs, lineDataSets, dateList ->
                    cm.makeLineChart(subUserLc, lineDataSets, dateList)
                    val dateFrom = cm.calculateDate(viewModel.chartDate!!, 1-viewModel.dateRange)//차트 시작일
                    dateRangeTv.text = dateFrom+"~"+viewModel.chartDate
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        moveRangeSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when(position){
                    0-> viewModel.moveRange = 30
                    1-> viewModel.moveRange = 10
                    2-> viewModel.moveRange = 5
                    3-> viewModel.moveRange = 1
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        dialog.show()
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

    fun showDialogMessage(title: String, body: String, callBack: () -> Unit) {//다이얼로그 메시지를 띄우는 함수
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(body)
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int -> callBack}
        builder.show()
    }
    fun showEditTextDialogMessage(title: String, callBack: (input: String) -> Unit) {//다이얼로그 메시지를 띄우는 함수
        val editText = EditText(this)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setView(editText)
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int -> callBack(editText.text.toString())}
        builder.setNegativeButton("취소") { dialogInterface: DialogInterface, i: Int -> }
        builder.show()
    }

    fun showTwoButtonDialogMessage(title: String, body: String, callBack: (Int) -> Unit) {//다이얼로그 메시지를 띄우는 함수
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(body)
        builder.setPositiveButton("확인") { dialogInterface: DialogInterface, i: Int -> callBack(i)}
        builder.setNegativeButton("취소") { dialogInterface: DialogInterface, i: Int -> callBack(i)}
        builder.show()
    }

    override fun onResume() {
        super.onResume()

        renewUI()
        mLoadingDialog.show()
        viewModel.reloadItems{
            mLoadingDialog.dismiss()
        }
    }
}