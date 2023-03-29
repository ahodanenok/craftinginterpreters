import sys
import os.path

def define_type(f, base_name, class_name, field_list):
    f.write('class {}({}):\n\n'.format(class_name, base_name))
    f.write('    def __init__(self, ' + field_list + '):\n')

    fields = field_list.split(', ')
    for field in fields:
        f.write('        self.{0} = {0}\n'.format(field))
    f.write('\n')

    f.write('    def accept(self, visitor):\n')
    f.write('        return visitor.visit{}{}(self)\n'.format(class_name, base_name))
    f.write('\n')

def define_visitor(f, base_name, types):
    f.write('class Visitor:\n\n')
    for type in types:
        type_name = type.split(':')[0].strip()
        f.write('    def visit{}{}({}):\n'.format(type_name, base_name, base_name.lower()))
        f.write('        pass\n\n')

def define_ast(output_dir, base_name, types):
    path = os.path.join(output_dir, base_name.lower() + '.py')
    with open(path, 'w', encoding = 'utf-8') as f:
        define_visitor(f, base_name, types)

        f.write('class ' + base_name + ':\n')
        f.write('    pass\n\n')
        for type in types:
            parts = type.split(':')
            class_name = parts[0].strip()
            fields = parts[1].strip()
            define_type(f, base_name, class_name, fields)
        f.write('')

if __name__ == '__main__':
    if len(sys.argv) != 2:
        sys.exit(64)
    define_ast(sys.argv[1], "Expr", [
        'Binary : left, operator, right',
        'Grouping : expression',
        'Literal : value',
        'Unary : operator, right'
    ])
