package security;

/**
 * Permissões do sistema — organizadas e sem duplicações.
 */
public enum Permission {

    // ===== AUDITORIA =====
    VIEW_AUDIT,

    // ===== USUÁRIOS =====
    MANAGE_USERS,        // acesso ao módulo de administração
    CREATE_USER,
    EDIT_USER,
    DELETE_USER,
    BLOCK_USER,
    RESET_PASSWORD,

    // ===== PERFIL DO PRÓPRIO USUÁRIO =====
    CHANGE_PASSWORD,

    // ===== MÁQUINAS =====
    VIEW_MACHINES,
    CREATE_MACHINE,
    EDIT_MACHINE,
    DELETE_MACHINE,

    // ===== MANUTENÇÕES =====
    VIEW_MAINTENANCE,
    CREATE_MAINTENANCE,
    EDIT_MAINTENANCE,
    DELETE_MAINTENANCE
}
