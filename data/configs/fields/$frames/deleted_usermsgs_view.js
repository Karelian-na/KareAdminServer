/**
 * @type FieldsConfig
 */
export default {
	id: {
		layoutSpan: 12,
		type: "text",
	},
	uid: {
		layoutSpan: 12,
		type: "text",
	},

	name: {
		layoutSpan: 12,
		type: "text",
		columnBindProps: {
			align: "left",
		},
	},
	avatar: {
		layoutSpan: 12,
		type: "image",
		bindProps: {
			autoUpload: false,
			limit: 1,
			showFileList: false,
		},
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
		rule: __COMMON_FIELDS_CONFIGS__.requiredRule,
		itemBindProps: {
			label: "角色:",
		},
	},

	add_time: {
		layoutSpan: 12,
		type: "text",
	},
	add_user: {
		layoutSpan: 12,
		type: "text",
	},

	delete_user: {
		layoutSpan: 12,
		type: "text",
	},
	delete_time: {
		layoutSpan: 12,
		type: "text",
	},
	delete_type: {
		layoutSpan: 12,
		type: "enum",
		enumItems: [
			{ label: "删除", value: 1 },
			{ label: "注销", value: 2 },
		],
	},
};
