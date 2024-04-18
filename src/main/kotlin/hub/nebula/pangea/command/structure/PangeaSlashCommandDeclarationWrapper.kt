package hub.nebula.pangea.command.structure

interface PangeaSlashCommandDeclarationWrapper {
    fun create(): PangeaSlashCommandDeclarationBuilder

    fun command(name: String, description: String, block: PangeaSlashCommandDeclarationBuilder.() -> Unit): PangeaSlashCommandDeclarationBuilder {
        return PangeaSlashCommandDeclarationBuilder(name, description).apply(block)
    }
}