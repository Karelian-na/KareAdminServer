/** @format */

/**
 * 在对话框模式为添加模式时，不显示字段，其余模式显示字段
 * @type FieldShowCallback
 */
const UnShowFieldWhenAdd = function (mode) {
	return mode !== "add";
};

/**
 * 始终不显示字段的回调
 * @type FieldShowCallback
 */
const UnShowField = function () {
	return false;
};

const statics = {
	user: {
		idPattern: "^[1-9]\\d{7}$",
		errorIdTip: "用户ID必须为不为0开头的8位数字",
		uidPattern: "^[a-zA-Z]\\w{5,20}$",
		errorUidPattern: "用户名必须满足^[a-zA-Z]\\w{5,20}$",
		politicalStatus: [
			{ value: 1, label: "中共党员" },
			{ value: 2, label: "中共预备党员" },
			{ value: 3, label: "共青团员" },
			{ value: 4, label: "民革党员" },
			{ value: 5, label: "民盟盟员" },
			{ value: 6, label: "民建会员" },
			{ value: 7, label: "民进会员" },
			{ value: 8, label: "农工党党员" },
			{ value: 9, label: "致公党党员" },
			{ value: 10, label: "九三学社社员" },
			{ value: 11, label: "台盟盟员" },
			{ value: 12, label: "无党派人士" },
			{ value: 13, label: "群众" },
		],
		clan: [
			{ value: 1, label: "汉族" },
			{ value: 2, label: "蒙古族" },
			{ value: 3, label: "回族" },
			{ value: 4, label: "藏族" },
			{ value: 5, label: "维吾尔族" },
			{ value: 6, label: "苗族" },
			{ value: 7, label: "彝族" },
			{ value: 8, label: "壮族" },
			{ value: 9, label: "布依族" },
			{ value: 10, label: "朝鲜族" },
			{ value: 11, label: "满族" },
			{ value: 12, label: "侗族" },
			{ value: 13, label: "瑶族" },
			{ value: 14, label: "白族" },
			{ value: 15, label: "土家族" },
			{ value: 16, label: "哈尼族" },
			{ value: 17, label: "哈萨克族" },
			{ value: 18, label: "傣族" },
			{ value: 19, label: "黎族" },
			{ value: 20, label: "僳僳族" },
			{ value: 21, label: "佤族" },
			{ value: 22, label: "畲族" },
			{ value: 23, label: "高山族" },
			{ value: 24, label: "拉祜族" },
			{ value: 25, label: "水族" },
			{ value: 26, label: "东乡族" },
			{ value: 27, label: "纳西族" },
			{ value: 28, label: "景颇族" },
			{ value: 29, label: "柯尔克孜族" },
			{ value: 30, label: "土族" },
			{ value: 31, label: "达斡尔族" },
			{ value: 32, label: "仫佬族" },
			{ value: 33, label: "羌族" },
			{ value: 34, label: "布朗族" },
			{ value: 35, label: "撒拉族" },
			{ value: 36, label: "毛南族" },
			{ value: 37, label: "仡佬族" },
			{ value: 38, label: "锡伯族" },
			{ value: 39, label: "阿昌族" },
			{ value: 40, label: "普米族" },
			{ value: 41, label: "塔吉克族" },
			{ value: 42, label: "怒族" },
			{ value: 43, label: "乌孜别克族" },
			{ value: 44, label: "俄罗斯族" },
			{ value: 45, label: "鄂温克族" },
			{ value: 46, label: "德昂族" },
			{ value: 47, label: "保安族" },
			{ value: 48, label: "裕固族" },
			{ value: 49, label: "京族" },
			{ value: 50, label: "塔塔尔族" },
			{ value: 51, label: "独龙族" },
			{ value: 52, label: "鄂伦春族" },
			{ value: 53, label: "赫哲族" },
			{ value: 54, label: "门巴族" },
			{ value: 55, label: "珞巴族" },
			{ value: 56, label: "基诺族" },
		],
	},
	menu: {
		oper_type: [
			{ value: 0, label: "不可操作(不是按钮级权限)" },
			{ value: 1, label: "仅可批量操作(仅显示在操作栏)" },
			{ value: 2, label: "仅可单独操作(仅显示在表格栏)" },
			{ value: 3, label: "即可整体操作也可单一操作(都显示)" },
		],
		type: [
			{ value: 1, label: "菜单" },
			{ value: 2, label: "选项" },
			{ value: 3, label: "标签" },
			{ value: 4, label: "操作" },
		],
	},
};

export const fields_config = {
	statics,
	UnShowFieldWhenAdd,
	UnShowField,

	/**
	 * @type FieldsConfig
	 */
	recordableFieldsConfig: {
		add_uid: {
			layoutSpan: 12,
			type: "text",
			show: UnShowFieldWhenAdd,
		},
		add_user: {
			layoutSpan: 12,
			type: "text",
			show: UnShowFieldWhenAdd,
		},
		add_time: {
			layoutSpan: 12,
			type: "text",
			show: UnShowFieldWhenAdd,
		},
	},

	/**
	 * @type FieldsConfig
	 */
	updatableFieldsConfig: {
		update_uid: {
			layoutSpan: 12,
			type: "text",
			show: UnShowFieldWhenAdd,
		},
		update_user: {
			layoutSpan: 12,
			type: "text",
			show: UnShowFieldWhenAdd,
		},
		update_time: {
			layoutSpan: 12,
			type: "text",
			show: UnShowFieldWhenAdd,
		},
	},

	requiredRule: {
		required: true,
		message: "必填项不能为空!",
		trigger: "blur",
	},
};

globalThis.__COMMON_FIELDS_CONFIGS__ = fields_config;
