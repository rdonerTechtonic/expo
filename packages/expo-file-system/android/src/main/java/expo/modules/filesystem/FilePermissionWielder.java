package expo.modules.filesystem;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import expo.core.interfaces.InternalModule;
import expo.interfaces.filesystem.FilePermissionWielderInterface;
import expo.interfaces.filesystem.Permission;

public class FilePermissionWielder implements FilePermissionWielderInterface, InternalModule {

  @Override
  public List<Class> getExportedInterfaces() {
    return Collections.<Class>singletonList(FilePermissionWielderInterface.class);
  }

  @Override
  public EnumSet<Permission> getPathPermissions(Context context, final String path) {
    EnumSet<Permission> permissions = getPermissionsIfPathIsInternal(path, context);
    if (permissions == null) {
      permissions = getPermissionsIfPathIsExternal(path);
    }
    return permissions;
  }

  protected  EnumSet<Permission> getPermissionsIfPathIsInternal(final String path, Context context) {
    try {
      String canonicalPath = new File(path).getCanonicalPath();
      for(String dir : getInternalPaths(context)) {
        if (canonicalPath.startsWith(dir + "/") || dir.equals(canonicalPath)) {
          return EnumSet.of(Permission.READ, Permission.WRITE);
        }
      }
    } catch (IOException e) {
      return EnumSet.noneOf(Permission.class);
    }
    return null;
  }

  protected EnumSet<Permission> getPermissionsIfPathIsExternal(final String path) {
    File file = new File(path);
    if (file.canWrite() && file.canRead()) {
      return EnumSet.of(Permission.READ, Permission.WRITE);
    }
    if (file.canWrite()) {
      return EnumSet.of(Permission.WRITE);
    }
    if (file.canRead()) {
      return EnumSet.of(Permission.READ);
    }
    return EnumSet.noneOf(Permission.class);
  }

  protected List<String> getInternalPaths(Context context) throws IOException {
    String filesDir = context.getFilesDir().getCanonicalPath();
    String cacheDir = context.getCacheDir().getCanonicalPath();
    return Arrays.asList(filesDir, cacheDir);
  }

}
