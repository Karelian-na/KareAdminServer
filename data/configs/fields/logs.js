/** @format */

/**
 * @type FieldsConfig
 */
export default {
	uid: {
		layoutSpan: 12,
		type: "text",
	},
	title: {
		layoutSpan: 12,
		type: "text",
	},

	url: {
		layoutSpan: 24,
		type: "text",
	},

	type: {
		layoutSpan: 12,
		type: "text",
	},
	date: {
		layoutSpan: 12,
		type: "text",
	},

	ip: {
		layoutSpan: 24,
		type: "text",
	},

	params: {
		layoutSpan: 24,
		type: "json",
		bindProps: {
			type: "textarea"
		}
	},
};
