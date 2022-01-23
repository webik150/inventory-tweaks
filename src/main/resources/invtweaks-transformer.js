function initializeCoreMod() {
    var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
    var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
    var IntInsnNode = Java.type("org.objectweb.asm.tree.IntInsnNode");
    var Type = Java.type("org.objectweb.asm.Type");
    var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
    var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");


    var ROW_SIZE_METHOD = "invtweaks$rowSize";

    return {
        'transformContainerMethod': {
                    'target': {
                        'type': 'CLASS',
                        'name': 'net.minecraft.world.inventory.AbstractMenuContainer'
                    },
                    'transformer': function(classNode) {
                        var methodNode = new MethodNode(Opcodes.ASM4, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, ROW_SIZE_METHOD, "()I", null, null);;
                        methodNode.instructions.add(new IntInsnNode(Opcodes.SIPUSH, 9));
                        methodNode.instructions.add(new InsnNode(Opcodes.IRETURN));
                        classNode.methods.add(methodNode);
                        console.error("Transforming menu container");
                        return classNode;
                    }
                },
        'coremodmethod': {
            'target': {
                'type': 'METHOD',
                'class': 'invtweaks.InvTweaksObfuscation',
                'methodName': 'getSpecialChestRowSize',
                'methodDesc': ROW_SIZE_METHOD
            },
            'transformer': function(method) {
                var methodType = Type.getMethodType(method.desc);
                var containertype = Type.getObjectType("net/minecraft/world/inventory/AbstractMenuContainer");
                console.error("Transforming chest row size");
                method.instructions.clear();
                method.instructions.add(new VarInsnNode(methodType.getOpcode(Opcodes.ILOAD), 0));
                method.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, methodType.getReturnType().getInternalName(), ROW_SIZE_METHOD, "()" + containertype.getDescriptor(), false));
                method.instructions.add(new InsnNode(methodType.getReturnType().getOpcode(Opcodes.IRETURN)));
                return method;
            }
        }
    }
}