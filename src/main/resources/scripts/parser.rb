require 'json'

def load_rxdata_safely(data)
  begin
    return Marshal.load(data)
  rescue ArgumentError => e
    missing_constant = e.message.match(/(uninitialized constant|undefined class\/module) (\S+)/)
    return "Error: Could not parse missing constant name from '#{e.message}'" unless missing_constant
    const_name = missing_constant[2]
    # STDERR.puts "DEBUG: Caught missing constant '#{const_name}'. Attempting to define it."
    parts = const_name.split('::')
    current_scope = Object
    parts.each_with_index do |part, index|
      is_defined = current_scope.const_defined?(part, false)
      unless is_defined
        if index < parts.size - 1
          current_scope.const_set(part, Module.new)
        else
          new_class = nil
          case part
          when "Tone", "Color"
            new_class = Class.new do
              attr_accessor :red, :green, :blue, :alpha
              def initialize(r, g, b, a = 255); @red, @green, @blue, @alpha = r, g, b, a; end
              def self._load(data); new(*data.unpack('d*')); end
              def as_json(options = {}); { '_class' => self.class.name, 'red' => @red, 'green' => @green, 'blue' => @blue, 'alpha' => @alpha }; end
            end
          when "Table"
            new_class = Class.new do
              attr_accessor :xsize, :ysize, :zsize, :data
              def initialize(xsize, ysize=1, zsize=1); @xsize, @ysize, @zsize = xsize, ysize, zsize; @data = Array.new(xsize * ysize * zsize, 0); end
              def self._load(data); header = data.unpack('L5'); table = new(header[1], header[2], header[3]); table.data = data[20..-1].unpack('S*'); table; end
              def as_json(options = {}); { '_class' => self.class.name, 'xsize' => @xsize, 'ysize' => @ysize, 'zsize' => @zsize, 'data' => @data }; end
            end
          else
            new_class = Class.new do
              def as_json(options = {}); json_hash = { '_class' => self.class.name }; self.instance_variables.each { |var| json_hash[var.to_s.delete('@')] = self.instance_variable_get(var) }; json_hash; end
            end
          end
          current_scope.const_set(part, new_class)
        end
      end
      current_scope = current_scope.const_get(part)
    end
    # STDERR.puts "DEBUG: Successfully defined '#{const_name}'. Retrying Marshal.load..."
    retry
  rescue => e
    return "Error: Failed to parse the Marshal file. Reason: #{e.message}"
  end
end

def deep_to_hash(obj, visited = {})
  if visited.key?(obj.object_id)
    return { '_ref' => obj.object_id, '_class' => obj.class.name }
  end

  visited[obj.object_id] = true

  result = nil
  if obj.respond_to?(:as_json) && !obj.is_a?(String) && !obj.is_a?(Numeric) && ![true, false, nil].include?(obj)
    hash = obj.as_json
    result = hash.transform_values { |value| deep_to_hash(value, visited) }
  elsif obj.is_a?(Array)
    result = obj.map { |item| deep_to_hash(item, visited) }
  elsif obj.is_a?(Hash)
    result = obj.transform_values { |value| deep_to_hash(value, visited) }
  else
    result = obj
  end

  visited.delete(obj.object_id)

  return result
end

file_path = rxdata_file_path

if !defined?(rxdata_file_path) || file_path.nil?
  'Error: No file path was provided to the script.'
elsif !File.exist?(file_path)
  "Error: File does not exist at path: #{file_path}"
else
  file_content = File.binread(file_path)
  parsed_data = load_rxdata_safely(file_content)

  if parsed_data.is_a?(String) && parsed_data.start_with?("Error:")
    parsed_data
  else
    converted_data = deep_to_hash(parsed_data)
    converted_data.to_json
  end
end