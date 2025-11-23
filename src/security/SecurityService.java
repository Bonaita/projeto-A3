package security;

import java.util.*;

/**
 * Serviço central de segurança.
 *
 * - Mantém mapa role -> permissions (in-memory)
 * - Exposes métodos para checar permissões e lançar exceções quando necessário
 * - Pode ser facilmente adaptado para ler do banco (DAO) ou arquivo JSON
 */
public class SecurityService {

    private static final Map<String, Set<Permission>> rolePerms = new HashMap<>();

    static {
        // configuração inicial — adapte conforme sua necessidade
        rolePerms.put(Role.ADMIN.name(), EnumSet.allOf(Permission.class));

        rolePerms.put(Role.MANAGER.name(), EnumSet.of(
                Permission.VIEW_MAINTENANCE,
                Permission.CREATE_MAINTENANCE,
                Permission.EDIT_MAINTENANCE,
                Permission.VIEW_MACHINES,
                Permission.VIEW_AUDIT // opcional para managers
        ));

        rolePerms.put(Role.USER.name(), EnumSet.of(
                Permission.VIEW_MAINTENANCE,
                Permission.VIEW_MACHINES,
                Permission.CHANGE_PASSWORD
        ));

        // também aceitar roles gravadas em minúsculas ou diferentes formatos
        // rolePerms.put("SOME_OTHER_ROLE", ...);
    }

    /**
     * Retorna se um role possui a permissão.
     */
    public boolean roleHasPermission(String role, Permission perm) {
        if (role == null) return false;
        Set<Permission> perms = rolePerms.get(role.toUpperCase());
        return perms != null && perms.contains(perm);
    }

    /**
     * Verifica e lança IllegalAccessException se não tiver permissão.
     * Use nos controllers/telas antes de executar ações críticas.
     */
    public void requirePermission(String role, Permission perm) {
        if (!roleHasPermission(role, perm)) {
            throw new SecurityException("Acesso negado: " + perm + " para role=" + role);
        }
    }

    /**
     * Permite registrar/atualizar permissões em runtime (útil para admin pages).
     */
    public void setRolePermissions(String role, Set<Permission> permissions) {
        rolePerms.put(role.toUpperCase(), EnumSet.copyOf(permissions));
    }

    /**
     * Recupera permissões do role.
     */
    public Set<Permission> getPermissions(String role) {
        return Collections.unmodifiableSet(rolePerms.getOrDefault(role.toUpperCase(), EnumSet.noneOf(Permission.class)));
    }
}
