/**
 * @type FieldsConfig
 */
const users = {
	id: {
		layoutSpan: 12,
		type: "text",
		rule: [
			__COMMON_FIELDS_CONFIGS__.requiredRule,
			{
				pattern: __COMMON_FIELDS_CONFIGS__.statics.user.idPattern,
				trigger: "blur",
				message: __COMMON_FIELDS_CONFIGS__.statics.user.errorIdTip,
			},
		],
	},
	uid: {
		layoutSpan: 12,
		type: "text",
		rule: [
			__COMMON_FIELDS_CONFIGS__.requiredRule,
			{
				pattern: __COMMON_FIELDS_CONFIGS__.statics.user.uidPattern,
				trigger: "blur",
				message: __COMMON_FIELDS_CONFIGS__.statics.user.errorUidPattern,
			},
		],
	},

	name: {
		layoutSpan: 12,
		type: "text",
		rule: __COMMON_FIELDS_CONFIGS__.requiredRule,
		columnBindProps: {
			showOverflowTooltip: false,
			align: "left",
		},
	},

	gender: {
		layoutSpan: 12,
		type: "enum",
		enumItems: [
			{ value: 1, label: "男" },
			{ value: 2, label: "女" },
		],
	},
	age: {
		layoutSpan: 12,
		type: "text",
	},

	email: {
		layoutSpan: 12,
		type: "text",
	},
	phone: {
		layoutSpan: 12,
		type: "text",
	},
};

/**
 * @type FieldsConfig
 */
const specialFieldsConfig = {
	avatar: {
		layoutSpan: 12,
		type: "image",
		bindProps: {
			autoUpload: false,
			limit: 1,
			showFileList: false,
		},
	},

	roles: {
		layoutSpan: 12,
		type: "text",
		rule: __COMMON_FIELDS_CONFIGS__.requiredRule,
	},

	political_status: {
		layoutSpan: 12,
		type: "enum",
		enumItems: __COMMON_FIELDS_CONFIGS__.statics.user.politicalStatus,
	},

	clan: {
		layoutSpan: 12,
		type: "enum",
		enumItems: __COMMON_FIELDS_CONFIGS__.statics.user.clan,
	},

	add_time: {
		layoutSpan: 12,
		type: "text",
	},
	profile: {
		layoutSpan: 24,
		type: "text",
		bindProps: {
			type: "textarea",
			resize: "none",
			rows: 3,
		},
		show: __COMMON_FIELDS_CONFIGS__.UnShowField,
	},
};

/**
 * @type FieldsConfig
 */
export default Object.assign({}, users, specialFieldsConfig, __COMMON_FIELDS_CONFIGS__.recordableFieldsConfig);
