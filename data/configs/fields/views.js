/** @format */

/**
 * @type FieldsConfig
 */
export default {
	view_name: {
		layoutSpan: 24,
		type: "text",
	},

	fields: {
		layoutSpan: 24,
		type: "custom",
		itemBindProps: {
			label: "字段列表:",
		},
	},

	comment: {
		layoutSpan: 24,
		type: "text",
		bindProps: {
			type: "textarea",
			rows: 3,
		},
	},

	fields_config: {
		layoutSpan: 24,
		type: "text",
		bindProps: {
			type: "textarea",
			rows: 12,
		},
		columnBindProps: {
			align: "left"
		}
	},

	update_time: {
		layoutSpan: 12,
		type: "text",
	},
	update_user: {
		layoutSpan: 12,
		type: "text",
	},
};
