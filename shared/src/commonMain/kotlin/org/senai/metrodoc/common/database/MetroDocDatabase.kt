package org.senai.metrodoc.common.database

import androidx.room3.*
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import org.senai.metrodoc.common.database.dao.ProjectDao
import org.senai.metrodoc.common.database.dao.VersionDao
import org.senai.metrodoc.common.database.entity.DrawShapeConverters
import org.senai.metrodoc.common.database.entity.MeasurementDataEntity
import org.senai.metrodoc.common.database.entity.ProjectEntity
import org.senai.metrodoc.common.database.entity.ReportDataEntity
import org.senai.metrodoc.common.database.entity.VersionEntity

val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        // 1. Criar a nova tabela de projetos
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS `projects` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `nomeProjeto` TEXT NOT NULL,
                `cliente` TEXT NOT NULL DEFAULT '',
                `componente` TEXT NOT NULL DEFAULT '',
                `createdAt` INTEGER NOT NULL DEFAULT 0,
                `lastModified` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // 2. Copiar projetos legados a partir dos relatórios existentes
        connection.execSQL("""
            INSERT INTO `projects` (`id`, `nomeProjeto`, `cliente`, `componente`, `createdAt`, `lastModified`)
            SELECT `id`, `nomeRelatorio`, `cliente`, `componente`, `lastModified`, `lastModified` FROM `reports`
        """.trimIndent())

        // 3. Recriar a tabela `reports` para incluir a Foreign Key para `projects(id)` e remover restrições antigas
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS `reports_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `projectId` INTEGER NOT NULL,
                `nomeRelatorio` TEXT NOT NULL,
                `pdfName` TEXT NOT NULL,
                `pdfPath` TEXT NOT NULL,
                `lastModified` INTEGER NOT NULL,
                `secoesJson` TEXT NOT NULL,
                `cliente` TEXT NOT NULL,
                `componente` TEXT NOT NULL,
                `identificadorCalypso` TEXT NOT NULL,
                `maquina` TEXT NOT NULL,
                `numeroMaquina` TEXT NOT NULL,
                `software` TEXT NOT NULL,
                `operador` TEXT NOT NULL,
                `dataHora` TEXT NOT NULL,
                `qtdCaracteristicas` TEXT NOT NULL,
                FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())

        connection.execSQL("""
            INSERT INTO `reports_new` (
                `id`, `projectId`, `nomeRelatorio`, `pdfName`, `pdfPath`, `lastModified`,
                `secoesJson`, `cliente`, `componente`, `identificadorCalypso`, `maquina`,
                `numeroMaquina`, `software`, `operador`, `dataHora`, `qtdCaracteristicas`
            )
            SELECT
                `id`, `id`, `nomeRelatorio`, `pdfName`, `pdfPath`, `lastModified`,
                `secoesJson`, `cliente`, `componente`, `identificadorCalypso`, `maquina`,
                `numeroMaquina`, `software`, `operador`, `dataHora`, `qtdCaracteristicas`
            FROM `reports`
        """.trimIndent())

        connection.execSQL("DROP TABLE `reports`")
        connection.execSQL("ALTER TABLE `reports_new` RENAME TO `reports`")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_reports_projectId` ON `reports` (`projectId`)")

        // 4. Recriar a tabela `versions` para atualizar a Foreign Key de `reports(id)` para `projects(id)`
        connection.execSQL("""
            CREATE TABLE IF NOT EXISTS `versions_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `projectId` INTEGER NOT NULL,
                `versionName` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `contentJson` TEXT NOT NULL,
                FOREIGN KEY(`projectId`) REFERENCES `projects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """.trimIndent())

        connection.execSQL("""
            INSERT INTO `versions_new` (`id`, `projectId`, `versionName`, `createdAt`, `contentJson`)
            SELECT `id`, `projectId`, `versionName`, `createdAt`, `contentJson` FROM `versions`
        """.trimIndent())

        connection.execSQL("DROP TABLE `versions`")
        connection.execSQL("ALTER TABLE `versions_new` RENAME TO `versions`")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_versions_projectId` ON `versions` (`projectId`)")

        // 5. Adicionar o índice na tabela `measurements`
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_measurements_reportId` ON `measurements` (`reportId`)")
    }
}

@Database(
    entities = [
        ProjectEntity::class,
        ReportDataEntity::class,
        MeasurementDataEntity::class,
        VersionEntity::class
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@ConstructedBy(AppDatabaseConstructor::class)
@ColumnTypeConverters(DrawShapeConverters::class)
abstract class MetroDocDatabase : RoomDatabase() {
    abstract fun getProjectDao(): ProjectDao

    abstract fun getVersionDao(): VersionDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<MetroDocDatabase> {
    override fun initialize(): MetroDocDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<MetroDocDatabase>
): MetroDocDatabase {
    return builder
        .addMigrations(MIGRATION_2_3)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}