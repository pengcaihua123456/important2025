import com.example.stepcopilot.`interface`.IGuiDialog
import com.example.stepcopilot.core.GuiFactory

class MyGuiDialog : IGuiDialog {
    override fun onTaskStart() {
        // 显示任务开始
    }

    override fun onTaskComplete(reason: String) {
        // 显示任务完成
    }

    override fun onTaskFail(reason: String) {
        // 显示任务失败
    }

    override fun onStepHint(hint: String) {
        // 更新当前步骤提示
    }

    override fun onListenUserBack(reasoning: String, text: String?) {
        // 等待用户补充信息，可弹窗让用户输入
    }

    override fun setState(state: GuiFactory.State) {
        // 更新 UI 状态
    }
}