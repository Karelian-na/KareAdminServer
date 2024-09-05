import { fields_config } from "common";

export interface InternalRuleItem extends Omit<RuleItem, "validator"> {
	field?: string;
	fullField?: string;
	fullFields?: string[];
	validator?: RuleItem["validator"] | ExecuteValidator;
}
declare type ValidateMessage<T extends any[] = unknown[]> = string | ((...args: T) => string);
export interface ValidateMessages {
	default?: ValidateMessage;
	required?: ValidateMessage<[FullField]>;
	enum?: ValidateMessage<[FullField, EnumString]>;
	whitespace?: ValidateMessage<[FullField]>;
	date?: {
		format?: ValidateMessage;
		parse?: ValidateMessage;
		invalid?: ValidateMessage;
	};
	types?: {
		string?: ValidateMessage<[FullField, Type]>;
		method?: ValidateMessage<[FullField, Type]>;
		array?: ValidateMessage<[FullField, Type]>;
		object?: ValidateMessage<[FullField, Type]>;
		number?: ValidateMessage<[FullField, Type]>;
		date?: ValidateMessage<[FullField, Type]>;
		boolean?: ValidateMessage<[FullField, Type]>;
		integer?: ValidateMessage<[FullField, Type]>;
		float?: ValidateMessage<[FullField, Type]>;
		regexp?: ValidateMessage<[FullField, Type]>;
		email?: ValidateMessage<[FullField, Type]>;
		url?: ValidateMessage<[FullField, Type]>;
		hex?: ValidateMessage<[FullField, Type]>;
	};
	string?: {
		len?: ValidateMessage<[FullField, Range]>;
		min?: ValidateMessage<[FullField, Range]>;
		max?: ValidateMessage<[FullField, Range]>;
		range?: ValidateMessage<[FullField, Range, Range]>;
	};
	number?: {
		len?: ValidateMessage<[FullField, Range]>;
		min?: ValidateMessage<[FullField, Range]>;
		max?: ValidateMessage<[FullField, Range]>;
		range?: ValidateMessage<[FullField, Range, Range]>;
	};
	array?: {
		len?: ValidateMessage<[FullField, Range]>;
		min?: ValidateMessage<[FullField, Range]>;
		max?: ValidateMessage<[FullField, Range]>;
		range?: ValidateMessage<[FullField, Range, Range]>;
	};
	pattern?: {
		mismatch?: ValidateMessage<[FullField, Value, Pattern]>;
	};
}
export interface ValidateOption {
	suppressWarning?: boolean;
	suppressValidatorError?: boolean;
	first?: boolean;
	firstFields?: boolean | string[];
	messages?: Partial<ValidateMessages>;
	/** The name of rules need to be trigger. Will validate all rules if leave empty */
	keys?: string[];
	error?: (rule: InternalRuleItem, message: string) => ValidateError;
}
export interface RuleItem {
	type?: RuleType;
	required?: boolean;
	pattern?: RegExp | string;
	min?: number;
	max?: number;
	len?: number;
	enum?: Array<string | number | boolean | null | undefined>;
	whitespace?: boolean;
	fields?: Record<string, Rule>;
	options?: ValidateOption;
	defaultField?: Rule;
	transform?: (value: Value) => Value;
	message?: string | ((a?: string) => string);
	asyncValidator?: (
		rule: InternalRuleItem,
		value: Value,
		callback: (error?: string | Error) => void,
		source: Values,
		options: ValidateOption
	) => void | Promise<void>;
	validator?: (
		rule: InternalRuleItem,
		value: Value,
		callback: (error?: string | Error) => void,
		source: Values,
		options: ValidateOption
	) => SyncValidateResult | void;
}
export interface FormItemRule extends RuleItem {
	trigger?: Arrayable<string>;
}

export declare type Arrayable<T> = T | T[];
declare type Primitive = null | undefined | string | number | boolean | symbol | bigint;
declare type BrowserNativeObject = Date | FileList | File | Blob | RegExp;
declare type PathImpl<K extends string | number, V> = V extends Primitive | BrowserNativeObject ? `${K}` : `${K}` | `${K}.${Path<V>}`;
declare type ArrayMethodKey = keyof any[];
declare type IsTuple<T extends ReadonlyArray<any>> = number extends T["length"] ? false : true;
declare type TupleKey<T extends ReadonlyArray<any>> = Exclude<keyof T, ArrayMethodKey>;
declare type Path<T> = T extends ReadonlyArray<infer V>
	? IsTuple<T> extends true
		? {
				[K in TupleKey<T>]-?: PathImpl<Exclude<K, symbol>, T[K]>;
		  }[TupleKey<T>]
		: PathImpl<ArrayKey, V>
	: {
			[K in keyof T]-?: PathImpl<Exclude<K, symbol>, T[K]>;
	  }[keyof T];
declare type FieldPath<T> = T extends object ? Path<T> : never;
export declare type FormRules<T extends Record<string, any> | string = string> = Partial<
	Record<T extends string ? T : FieldPath<T>, Arrayable<FormItemRule>>
>;

export declare global {
	type EditItemType =
		| "text"
		| "number"
		| "date"
		| "time"
		| "enum"
		| "image"
		| "file"
		| "radio"
		| "switch"
		| "custom"
		| "checkbox"
		| "json";

	/**
	 * @description 字段配置接口
	 * @author Karelian_na
	 * @date 2023/09/14 09:17:34
	 * @export
	 * @interface IFieldConfig
	 */
	interface IFieldConfig {
		/**
		 * @description 绑定到 EditItem 组件的额外属性，具体于指定类型对应的element组件的属性，例如当{@link EditItemType}="enum"时，为ElSelect
		 * @author Karelian_na
		 * @date 2023/09/08 20:21:02
		 * @type {*}
		 */
		bindProps?: any;

		/**
		 * @description 绑定到EditItem组件的额外属性，转发至ElFormItem
		 * @author Karelian_na
		 * @date 2023/09/13 17:07:43
		 * @type {Partial<FormItemInstance["$props"]>}
		 */
		itemBindProps?: Partial<FormItemInstance["$props"]>;

		/**
		 * @description 字段在编辑或详情框中所占的宽度大小，最大24即占满一行
		 * @author Karelian_na
		 * @date 2023/09/08 20:19:09
		 * @type {number}
		 */
		layoutSpan: number;

		/**
		 * @description 当前字段的类型
		 * @author Karelian_na
		 * @date 2023/09/08 20:18:36
		 * @type {EditItemType}
		 */
		type: EditItemType;

		/**
		 * @description 字段的表单检验规则
		 * @author Karelian_na
		 * @date 2023/09/08 20:18:19
		 * @type {FormRules[""]}
		 */
		rule?: FormRules[""];

		/**
		 * @description 字段类型 {@link EditItemType} = "enum" 时，字段可供选择的值的集合
		 * @author Karelian_na
		 * @date 2023/09/08 20:16:27
		 * @type {IEnumItem[]}
		 */
		enumItems?: Array<IEnumItem>;

		/**
		 * @description 依赖本字段的字段
		 * @author Karelian_na
		 * @date 2023/09/08 20:15:50
		 * @type {FieldsConfig}
		 */
		children?: FieldsConfig;

		/**
		 * @description 给定编辑或显示详情时是否显示该字段的回调
		 * @author Karelian_na
		 * @date 2023/09/08 20:15:12
		 * @type {FieldShowCallback}
		 */
		show?: FieldShowCallback | boolean;

		/**
		 * @description 绑定到ElTableColumn组件的其它属性
		 * @author Karelian_na
		 * @date 2023/09/08 20:14:17
		 * @type {TableColumnInstance["$props"]}
		 */
		columnBindProps?: TableColumnInstance["$props"];
	}

	/**
	 * 字段配置集
	 */
	type FieldsConfig = Record<string, IFieldConfig>;

	/**
	 * 编辑、添加对话中决定字段是否显示的回调
	 */
	type FieldShowCallback = (mode: string) => boolean;

	const useFormData: () => Record<string, any>;

	var __COMMON_FIELDS_CONFIGS__: typeof fields_config;
}
