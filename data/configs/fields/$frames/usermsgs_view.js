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

	roles: {
		layoutSpan: 24,
		type: "text",
		show: (mode) => mode !== "add",
	},

	roles_id: {
		layoutSpan: 24,
		type: "enum",
		show: (mode) => mode === "add",
		bindProps: {
			multiple: true,
		},
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
			accept: ".png,.jpg,.jpeg",
		},
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
export default Object.assign(
	{},
	users,
	specialFieldsConfig,
	__COMMON_FIELDS_CONFIGS__.recordableFieldsConfig
);
