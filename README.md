## 手机端蓝牙串口
添加控制命令
1. 在`com.baiye959.ble.data.command.impl`里添加命令函数命名及其传参和返回参数
2. 更改`com/baiye959/ble/data/command/Commands.kt`，添加新增的命令

添加状态数据（目前只添加了get_current_fps状态数据）
1. 在`com/baiye959/ble/data/model/UiState.kt`里更改`DeviceUiState`，添加需要展示的状态（建议使用string类型）
2. 更改`com/baiye959/ble/viewmodel/DeviceViewModel.kt`里的`handleStatusMessage`函数 
3. 更改`com/baiye959/ble/ui/components/DeviceComponents.kt`里的`DeviceStatusCard`组件，新增参数并在末尾添加Text展示 
4. 更改`com/baiye959/ble/ui/screen/DeviceScreen.kt`里的UserScreen，新增DeviceStatusCard传参
