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
					if (value < 3 || value > 126) {
						callback("角色等级只能介于3-126之间!");
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
