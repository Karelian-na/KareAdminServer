/** @type {FieldsConfig} */
const specialPermissionsFieldsConfig = {
	id: {
		type: "text",
		layoutSpan: 8,
	},
	name: {
		type: "text",
		layoutSpan: 16,
		rule: __COMMON_FIELDS_CONFIGS__.requiredRule,
	},

	oper_id: {
		type: "text",
		layoutSpan: 24,
		rule: __COMMON_FIELDS_CONFIGS__.requiredRule,
	},

	oper_type: {
		layoutSpan: 16,
		type: "enum",
		enumItems: __COMMON_FIELDS_CONFIGS__.statics.menu.oper_type,
		rule: __COMMON_FIELDS_CONFIGS__.requiredRule,
	},
	status: {
		type: "switch",
		layoutSpan: 8,
		enumItems: [
			{ value: true, label: "启用" },
			{ value: false, label: "禁用" },
		],
		bindProps: {
			"inline-prompt": true,
			"active-text": "启用",
			"inactive-text": "禁用",
		},
	},

	descrip: {
		layoutSpan: 24,
		type: "text",
		rule: {
			max: 100,
			message: "长度不可超过100字!",
			trigger: "blur",
		},
		bindProps: {
			type: "textarea",
			rows: 3,
			resize: "none",
		},
	},
};

export default Object.assign(
	specialPermissionsFieldsConfig,
	__COMMON_FIELDS_CONFIGS__.recordableFieldsConfig,
	__COMMON_FIELDS_CONFIGS__.updatableFieldsConfig
);
