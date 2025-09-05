require 'json'
require 'zlib'

class RxDataContainer
end

def convert_nested_data(obj)
  case obj
  when Hash
    obj.each_with_object({}) do |(key, value), new_hash|
      new_key = key.match?(/\A\d+\z/) ? key.to_i : key.to_sym
      new_hash[new_key] = convert_nested_data(value)
    end
  when Array
    obj.map { |value| convert_nested_data(value) }
  else
    obj
  end
end

begin
  json_path = json_file_path
  output_path = rxdata_output_path

  top_level_hash = JSON.parse(File.read(json_path))

  final_object = RxDataContainer.new

  top_level_hash.each do |key, value|
    instance_var_name = "@#{key}"

    instance_var_value = convert_nested_data(value)

    final_object.instance_variable_set(instance_var_name, instance_var_value)
  end

  marshalled_data = Marshal.dump(final_object)

  File.open(output_path, 'wb') do |file|
    file.write(marshalled_data)
  end

rescue => e
  puts "Error during final object generation: #{e.message}"
end