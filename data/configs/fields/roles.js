/** @format */

/**
 * @type FieldsConfig
 */
const specialFieldsConfig = {
	name: {
		layoutSpan: 12,
		type: "text",
		rule: __COMMON_FIELDS_CONFIGS__.requiredRule,
	},
	level: {
		layoutSpan: 12,
		type: "number",
		rule: [
			__COMMON_FIELDS_CONFIGS__.requiredRule,
			{
				validator(rule, value, callback, source, options) {
					if (value < 2 || value > 100) {
						callback("角色等级只能介于2-100之间!");
						return;
					}
					callback();
				},
				trigger: "blur",
			},
		],
		columnBindProps: {
			sortable: true,
		},
	},

	descrip: {
		layoutSpan: 24,
		type: "text",
		bindProps: {
			type: "textarea",
			rows: 3,
			resize: "none",
		},
	},
};

export default Object.assign(
	specialFieldsConfig,
	__COMMON_FIELDS_CONFIGS__.recordableFieldsConfig,
	__COMMON_FIELDS_CONFIGS__.updatableFieldsConfig
);
